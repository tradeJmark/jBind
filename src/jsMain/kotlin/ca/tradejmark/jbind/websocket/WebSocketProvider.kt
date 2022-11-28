package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.ValueLocation
import ca.tradejmark.jbind.websocket.Serialization.deserializeServerMessage
import ca.tradejmark.jbind.websocket.Serialization.serializeMessage
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.w3c.dom.WebSocket

class WebSocketProvider(webSocket: WebSocket? = null): Provider {
    private val webSocket = webSocket ?: WebSocket("ws://${window.location.host}")
    private val directory = mutableMapOf<ValueLocation, MutableStateFlow<String?>>()
    private val arrayDirectory = mutableMapOf<ObjectLocation, MutableStateFlow<Int?>>()

    init {
        this.webSocket.onmessage = { event ->
            when (val message = deserializeServerMessage(event.data.toString())) {
                is ValueResponse -> {
                    directory[message.location]?.tryEmit(message.value)
                }
                is ArrayLengthResponse -> {
                    arrayDirectory[message.location]?.tryEmit(message.length)
                }
                is WSProviderError -> {
                    console.error("Server: ${message.msg}")
                }
            }
        }
    }

    override fun getValue(location: ValueLocation): StateFlow<String?> {
        val flow = directory.getOrPut(location) { MutableStateFlow(null) }
        val message = serializeMessage(ValueRequest(location))
        webSocket.send(message)
        return flow
    }

    override fun getArrayLength(location: ObjectLocation): StateFlow<Int?> {
        val flow = arrayDirectory.getOrPut(location) { MutableStateFlow(null) }
        val message = serializeMessage(ArrayLengthRequest(location))
        webSocket.send(message)
        return flow
    }
}