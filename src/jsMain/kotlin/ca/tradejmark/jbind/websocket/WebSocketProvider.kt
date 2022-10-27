package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.ValueLocation
import ca.tradejmark.jbind.websocket.Serialization.deserializeServerMessage
import ca.tradejmark.jbind.websocket.Serialization.serializeMessage
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.w3c.dom.WebSocket

class WebSocketProvider(webSocket: WebSocket? = null): Provider {
    private val webSocket = webSocket ?: WebSocket("ws://${window.location.host}")
    private val directory = mutableMapOf<ValueLocation, MutableList<MutableStateFlow<String?>>>()
    private val arrayDirectory = mutableMapOf<ObjectLocation, MutableList<MutableStateFlow<Int?>>>()

    init {
        this.webSocket.onmessage = { event ->
            when (val message = deserializeServerMessage(event.data.toString())) {
                is ValueResponse -> {
                    directory[message.location]?.forEach { it.tryEmit(message.value) }
                }
                is ArrayLengthResponse -> {
                    arrayDirectory[message.location]?.forEach { it.tryEmit(message.length) }
                }
                is WSProviderError -> {
                    console.error("Server: ${message.msg}")
                }
            }
        }
    }

    override fun getValue(location: ValueLocation): Flow<String> {
        val flow = MutableStateFlow<String?>(null)
        (directory[location] ?: mutableListOf<MutableStateFlow<String?>>().apply { directory[location] = this }).add(flow)
        val message = serializeMessage(ValueRequest(location))
        webSocket.send(message)
        return flow.filterNotNull()
    }

    override fun getArrayLength(location: ObjectLocation): Flow<Int> {
        val flow = MutableStateFlow<Int?>(null)
        (arrayDirectory[location] ?: mutableListOf<MutableStateFlow<Int?>>().apply { arrayDirectory[location] = this }).add(flow)
        val message = serializeMessage(ArrayLengthRequest(location))
        webSocket.send(message)
        return flow.filterNotNull()
    }
}