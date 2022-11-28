package ca.tradejmark.jbind.server

import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.location.Location
import ca.tradejmark.jbind.websocket.*
import ca.tradejmark.jbind.websocket.Serialization.deserializeClientMessage
import ca.tradejmark.jbind.websocket.Serialization.serializeMessage
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

fun Route.jBind(provider: Provider) {
    application.pluginOrNull(WebSockets)
        ?: throw IllegalStateException("Can't run jBind without WebSockets plugin installed")
    val valueRegistry = mutableMapOf<Location, MutableSet<WebSocketServerSession>>()
    val arrayRegistry = mutableMapOf<Location, MutableSet<WebSocketServerSession>>()
    val valueJobs = mutableMapOf<Location, Job>()
    val arrayJobs = mutableMapOf<Location, Job>()

    webSocket {
        val locations = mutableSetOf<Location>()
        incoming.consumeAsFlow()
            .filterIsInstance<Frame.Text>()
            .map { deserializeClientMessage(it.readText()) }
            .collect { msg ->
                when (msg) {
                    is ValueRequest -> {
                        valueRegistry.getOrPut(msg.location) { mutableSetOf() }.add(this)
                        locations.add(msg.location)
                        if (valueJobs.contains(msg.location)) {
                            provider.getValue(msg.location).value?.let { value ->
                                send(serializeMessage(ValueResponse(msg.location, value)))
                            }
                        }
                        else {
                            val job = launch {
                                provider.getValue(msg.location).filterNotNull().collect { value ->
                                    valueRegistry[msg.location]?.forEach {
                                        it.send(serializeMessage(ValueResponse(msg.location, value)))
                                    }
                                }
                            }
                            valueJobs[msg.location] = job
                        }
                    }
                    is ArrayLengthRequest -> {
                        arrayRegistry.getOrPut(msg.location) { mutableSetOf() }.add(this)
                        locations.add(msg.location)
                        if (arrayJobs.contains(msg.location)) {
                            provider.getArrayLength(msg.location).value?.let { value ->
                                send(serializeMessage(ArrayLengthResponse(msg.location, value)))
                            }
                        }
                        else {
                            val job = launch {
                                provider.getArrayLength(msg.location).filterNotNull().collect { length ->
                                    arrayRegistry[msg.location]?.forEach {
                                        it.send(serializeMessage(ArrayLengthResponse(msg.location, length)))
                                    }
                                }
                            }
                            arrayJobs[msg.location] = job
                        }
                    }
                    is WSProviderError -> application.environment.log.error(msg.msg)
                }
            }
        for (location in locations) {
            var set = valueRegistry[location]
            set?.remove(this)
            if (set?.isEmpty() == true) {
                valueJobs[location]?.cancel()
            }
            set = arrayRegistry[location]
            set?.remove(this)
            if (set?.isEmpty() == true) {
                arrayJobs[location]?.cancel()
            }
        }
    }
}