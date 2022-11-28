import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.UnavailableError
import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.Path
import ca.tradejmark.jbind.location.ValueLocation
import ca.tradejmark.jbind.server.jBind
import ca.tradejmark.jbind.websocket.Serialization.deserializeServerMessage
import ca.tradejmark.jbind.websocket.Serialization.serializeMessage
import ca.tradejmark.jbind.websocket.ValueRequest
import ca.tradejmark.jbind.websocket.ValueResponse
import io.ktor.client.plugins.websocket.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

class KtorPluginTest {
    companion object {
        const val INITIAL = "initial"
    }
    private val testFlow = MutableStateFlow(INITIAL)

    @Test
    fun testKtorPlugin() {
        testApplication {
            application {
                install(io.ktor.server.websocket.WebSockets)
                routing {
                    jBind(object: Provider {
                        override fun getValue(location: ValueLocation): StateFlow<String> {
                            if (location == Path("test").obj("obj").value("val")) {
                                return  testFlow
                            }
                            else throw UnavailableError(location)
                        }

                        override fun getArrayLength(location: ObjectLocation): StateFlow<Int> { throw UnavailableError(location) }
                    })
                }
            }
            val cli = createClient {
                install(io.ktor.client.plugins.websocket.WebSockets)
            }
            cli.webSocket("/") {
                val msg = serializeMessage(ValueRequest(Path("test").obj("obj").value("val")))
                send(msg)

                val recvd = (deserializeServerMessage((incoming.receive() as Frame.Text).readText()) as ValueResponse).value
                assertEquals(INITIAL, recvd)
                val second = "second"
                testFlow.emit(second)
                val rec2 = (deserializeServerMessage((incoming.receive() as Frame.Text).readText()) as ValueResponse).value
                assertEquals(second, rec2)
            }
        }
    }
}