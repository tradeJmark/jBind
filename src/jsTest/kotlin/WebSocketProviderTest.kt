package ca.tradejmark.jbind

import ca.tradejmark.jbind.TestUtils.delayForUpdate
import ca.tradejmark.jbind.binds.TextBind.bindText
import ca.tradejmark.jbind.location.BindPath
import ca.tradejmark.jbind.location.BindValueLocation
import ca.tradejmark.jbind.websocket.Serialization.deserializeClientMessage
import ca.tradejmark.jbind.websocket.Serialization.serializeMessage
import ca.tradejmark.jbind.websocket.WSProviderError
import ca.tradejmark.jbind.websocket.WSProviderRequest
import ca.tradejmark.jbind.websocket.WSProviderResponse
import ca.tradejmark.jbind.websocket.WebSocketProvider
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.dom.clear
import org.w3c.dom.MessageEvent
import org.w3c.dom.WebSocket
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.serialization.json.JsonNames
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

@ExperimentalCoroutinesApi
class WebSocketProviderTest {
    @BeforeTest
    fun emptyBody() {
        document.body!!.clear()
    }

    @Test
    fun testWebSocketProvider() = runTest {
        val location = BindPath("test").obj("obj").value("val")
        val mockWS = object {
            private val followed = mutableSetOf<BindValueLocation>()
            @JsName("send")
            fun send(string: String) {
                when (val msg = deserializeClientMessage(string)) {
                    is WSProviderRequest -> followed.add(msg.location)
                    is WSProviderError -> fail("Client sent error.")
                }
            }

            fun sendValue(value: String) {
                followed.forEach {
                    val event = object {
                        @JsName("data")
                        val data = serializeMessage(WSProviderResponse(it, value))
                    }.unsafeCast<MessageEvent>()
                    onmessage?.invoke(event) ?: fail("No onmessage callback.")
                }
            }

            @JsName("onmessage")
            var onmessage: (MessageEvent.() -> Unit)? = null
        }
        val provider = WebSocketProvider(mockWS.unsafeCast<WebSocket>())
        val testDiv = document.body!!.append.div {
            bindText(location)
        }
        JBind.bind(document.body!!, provider)
        val first = "first"
        mockWS.sendValue(first)
        delayForUpdate()
        assertEquals(first, testDiv.innerText)
        val second = "second"
        mockWS.sendValue(second)
        delayForUpdate()
        assertEquals(second, testDiv.innerText)
    }
}