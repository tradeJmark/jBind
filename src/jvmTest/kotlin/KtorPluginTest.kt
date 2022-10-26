import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.UnavailableError
import ca.tradejmark.jbind.location.Path
import ca.tradejmark.jbind.location.ValueLocation
import ca.tradejmark.jbind.server.JBind
import ca.tradejmark.jbind.server.JBind.Feature.jBind
import ca.tradejmark.jbind.websocket.Serialization.deserializeServerMessage
import ca.tradejmark.jbind.websocket.Serialization.serializeMessage
import ca.tradejmark.jbind.websocket.WSProviderRequest
import ca.tradejmark.jbind.websocket.WSProviderResponse
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorPluginTest {
    companion object {
        const val INITIAL = "initial"
    }
    private val testFlow = MutableStateFlow(INITIAL)
    private val testApp: Application.() -> Unit = {
        install(JBind)
        routing {
            jBind(object: Provider {
                override fun getValue(location: ValueLocation): Flow<String> {
                    if (location == Path("test").obj("obj").value("val")) {
                        return  testFlow
                    }
                    else throw UnavailableError(location)
                }
            })
        }
    }

    @Test
    fun testKtorPlugin() {
        withTestApplication(testApp) {
            handleWebSocketConversation("/") { incoming, outgoing ->
                val msg = serializeMessage(WSProviderRequest(Path("test").obj("obj").value("val")))
                outgoing.send(Frame.Text(msg))

                val recvd = (deserializeServerMessage((incoming.receive() as Frame.Text).readText()) as WSProviderResponse).value
                assertEquals(INITIAL, recvd)
                val second = "second"
                testFlow.emit(second)
                val rec2 = (deserializeServerMessage((incoming.receive() as Frame.Text).readText()) as WSProviderResponse).value
                assertEquals(second, rec2)
            }
        }
    }
}