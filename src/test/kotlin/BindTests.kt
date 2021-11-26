package ca.tradejmark.jbind

import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlin.test.Test
import ca.tradejmark.jbind.DSL.bindToItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.html.dom.create
import kotlinx.html.js.body
import org.w3c.dom.get
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class BindTests {
    object TestProvider: Provider {
        const val INITIAL = "init"

        val testFlow = MutableStateFlow(INITIAL)
        override fun getString(location: BindLoc): Flow<String> {
            if (location.path.size == 1 && location.path[0] == "test" && location.obj == "object" && location.attr == "attr") {
                return  testFlow
            }
            else throw UnavailableError(location)
        }
    }

    @BeforeTest
    fun clearTestFlow() {
        TestProvider.testFlow.tryEmit(TestProvider.INITIAL)
    }

    @BeforeTest
    fun makeBody() {
        document.body = document.create.body()
    }

    @Test
    fun testItemBinding() = GlobalScope.launch(StandardTestDispatcher()) {
        val testDiv = document.body!!.append.div {
            bindToItem("test.object.attr")
        }
        bind(document.body!!, TestProvider)

        assertEquals(TestProvider.INITIAL, testDiv.innerText)
        val new = "new"
        TestProvider.testFlow.emit(new)
        assertEquals(new, testDiv.innerText)
    }
}