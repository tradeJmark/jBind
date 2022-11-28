package ca.tradejmark.jbind.server

import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.location.Location
import ca.tradejmark.jbind.websocket.Serialization.deserializeClientMessage
import ca.tradejmark.jbind.websocket.ValueRequest
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException


fun Route.jBind(provider: Provider) {
    application.pluginOrNull(WebSockets)
        ?: throw IllegalStateException("Can't run jBind without WebSockets plugin installed")
    val registry = mutableMapOf<Location, MutableSet<WebSocketServerSession>>()

    fun WebSocketServerSession.handleValueRequest(location: Location) {
        registry.getOrPut(location) { mutableSetOf() }.add(this)
        locations.add(cliMsg.location)
        if (jBind.valueJobs.contains(cliMsg.location)) {
            val currentValue = provider.getValue(cliMsg.location).value
            val response = ValueResponse(cliMsg.location, currentValue)
            send(serializeMessage(response))
        } else {
            val job = launch {
                provider.getValue(cliMsg.location).collect { newVal ->
                    val message = serializeMessage(ValueResponse(cliMsg.location, newVal))
                    jBind.registry[cliMsg.location]?.forEach { it.send(message) }
                }
            }
            jBind.valueJobs[cliMsg.location] = job
        }
    }

    webSocket {
        val locations = mutableSetOf<Location>()
        for (frame in incoming) {
            try {
                when (frame) {
                    is Frame.Text -> {
                        when (val cliMsg = deserializeClientMessage(frame.readText())) {
                            is ValueRequest -> handleValueRequest(cliMsg.location)

                            is ArrayLengthRequest -> {
                                if (jBind.handled[this]?.contains(cliMsg.location) != true) {
                                    launch {
                                        try {
                                            provider.getArrayLength(cliMsg.location).collect {
                                                try {
                                                    send(serializeMessage(ArrayLengthResponse(cliMsg.location, it)))
                                                } catch (e: ClosedReceiveChannelException) {
                                                    jBind.handled.remove(this@webSocket)
                                                }
                                            }
                                        } catch (e: UnavailableError) {
                                            send(serializeMessage(WSProviderError(e.message!!)))
                                        }
                                    }
                                }
                            }

                            is WSProviderError -> application.environment.log.error(cliMsg.msg)
                            else -> throw IllegalStateException("Compiler is wrong, this when is exhaustive")
                        }
                    }
                    else -> send(serializeMessage(WSProviderError("Cannot handle data other than text.")))
                }
            }
            catch (ex: ClosedReceiveChannelException) {

            }
        }
    }
}