package ca.tradejmark.jbind.server

import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.UnavailableError
import ca.tradejmark.jbind.location.Location
import ca.tradejmark.jbind.location.ValueLocation
import ca.tradejmark.jbind.websocket.*
import ca.tradejmark.jbind.websocket.Serialization.deserializeClientMessage
import ca.tradejmark.jbind.websocket.Serialization.serializeMessage
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.websocket.*
import io.ktor.websocket.WebSockets.WebSocketOptions
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

class JBind {
    internal val handled = mutableMapOf<WebSocketServerSession, MutableList<Location>>()
    class Configuration {
        internal var wsBlock: WebSocketOptions.() -> Unit = {}
        fun webSocket(block: WebSocketOptions.() -> Unit) { wsBlock = block }
    }

    companion object Feature: ApplicationFeature<Application, Configuration, JBind> {
        override val key = AttributeKey<JBind>("JBind")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): JBind {
            val c = Configuration().apply(configure)
            pipeline.featureOrNull(WebSockets) ?: pipeline.install(WebSockets, c.wsBlock)
            return JBind()
        }

        fun Route.jBind(provider: Provider) = webSocket {
            val jBind = application.feature(JBind)
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        when (val cliMsg = deserializeClientMessage(frame.readText())) {
                            is ValueRequest -> {
                                if (jBind.handled[this]?.contains(cliMsg.location) != true) {
                                    launch {
                                        try {
                                            provider.getValue(cliMsg.location).collect {
                                                try {
                                                    send(serializeMessage(ValueResponse(cliMsg.location, it)))
                                                } catch (e: ClosedReceiveChannelException) {
                                                    jBind.handled.remove(this@webSocket)
                                                }
                                            }
                                        }
                                        catch (e: UnavailableError) {
                                            send(serializeMessage(WSProviderError(e.message!!)))
                                        }
                                    }
                                    if (!jBind.handled.containsKey(this)) jBind.handled[this] = mutableListOf()
                                    jBind.handled[this]!!.add(cliMsg.location)
                                }
                            }
                            is ArrayLengthRequest -> {
                                if (jBind.handled[this]?.contains(cliMsg.location) != true) {
                                    launch {
                                        try {
                                            provider.getArrayLength(cliMsg.location).collect {
                                                try {
                                                    send(serializeMessage(ArrayLengthResponse(cliMsg.location, it)))
                                                }
                                                catch (e: ClosedReceiveChannelException) {
                                                    jBind.handled.remove(this@webSocket)
                                                }
                                            }
                                        }
                                        catch (e: UnavailableError) {
                                            send(serializeMessage(WSProviderError(e.message!!)))
                                        }
                                    }
                                }
                            }
                            is WSProviderError -> application.environment.log.error(cliMsg.msg)
                            else -> throw IllegalStateException("Compiler is wrong, this when is exhaustive")
                        }
                    }
                    is Frame.Close -> jBind.handled.remove(this)
                    else -> send(serializeMessage(WSProviderError("Cannot handle data other than text.")))
                }
            }
        }
    }
}