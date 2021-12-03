package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.location.BindValueLocation
import ca.tradejmark.jbind.websocket.Serialization.deserializeServerMessage
import ca.tradejmark.jbind.websocket.Serialization.serializeMessage
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.w3c.dom.WebSocket

class WebSocketProvider(webSocket: WebSocket? = null): Provider {
    private val webSocket = webSocket ?: WebSocket("ws://${window.location.host}")
    private val directory = mutableMapOf<BindValueLocation, MutableList<MutableStateFlow<String?>>>()

    init {
        this.webSocket.onmessage = { event ->
            when (val message = deserializeServerMessage(event.data.toString())) {
                is WSProviderResponse -> {
                    directory[message.location]?.forEach { it.tryEmit(message.value) }
                }
                is WSProviderError -> {
                    console.error("Server: ${message.msg}")
                }
            }
        }
    }

    override fun getValue(location: BindValueLocation): Flow<String> {
        val flow = MutableStateFlow<String?>(null)
        (directory[location] ?: mutableListOf<MutableStateFlow<String?>>().apply { directory[location] = this }).add(flow)
        val message = serializeMessage(WSProviderRequest(location))
        webSocket.send(message)
        return flow.filterNotNull()
    }
}