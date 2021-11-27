package ca.tradejmark.jbind

import ca.tradejmark.jbind.AttributesBind.bindAttributes
import ca.tradejmark.jbind.TextBind.bindText
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.html.dom.create
import kotlinx.html.js.body
import org.w3c.dom.get
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class BindTests {
    object TestProvider: Provider {
        const val INITIAL = "init"
        const val INITIAL2 = "2init2"

        val testFlow = MutableStateFlow(INITIAL)
        val testFlow2 = MutableStateFlow(INITIAL2)
        override fun getString(location: BindLoc): Flow<String> {
            if (location.path.size == 1 && location.path[0] == "test" && location.obj == "object" && location.attr == "attr") {
                return  testFlow
            }
            if (location.path.size == 2 && location.path[0] == "test" && location.path[1] == "inner" && location.obj == "obj" && location.attr == "attr") {
                return testFlow2
            }
            else throw UnavailableError(location)
        }
    }

    @BeforeTest
    fun clearTestFlows() {
        TestProvider.testFlow.tryEmit(TestProvider.INITIAL)
        TestProvider.testFlow2.tryEmit(TestProvider.INITIAL2)
    }

    @Test
    fun testTextBinding() = TestScope().launch {
        val testDiv = document.body!!.append.div {
            bindText(BindLoc(listOf("test"), "obj", "attr"))
        }
        JBind.bind(document.body!!, TestProvider)

        assertEquals(TestProvider.INITIAL, testDiv.innerText)
        val new = "new"
        TestProvider.testFlow.emit(new)
        assertEquals(new, testDiv.innerText)
    }

    @Test
    fun testAttributeBinding() = TestScope().launch {
        val testDiv = document.body!!.append.div {
            bindAttributes(mapOf(
                "testA" to BindLoc(listOf("test"), "obj", "attr"),
                "testB" to BindLoc(listOf("test", "inner"), "obj", "attr")
            ))
        }
        JBind.bind(document.body!!, TestProvider)

        assertEquals(TestProvider.INITIAL, testDiv.getAttribute("testA"))
        assertEquals(TestProvider.INITIAL2, testDiv.getAttribute("testB"))
        val second = "second"
        TestProvider.testFlow.emit(second)
        assertEquals(second, testDiv.getAttribute("testA"))
        assertEquals(TestProvider.INITIAL2, testDiv.getAttribute("testB"))
        val third = "third"
        TestProvider.testFlow.emit(third)
        TestProvider.testFlow2.emit(third)
        assertEquals(third, testDiv.getAttribute("testA"))
        assertEquals(third, testDiv.getAttribute("testB"))
    }

    @Test
    fun testAllBindings() = TestScope().launch {
        val testDiv = document.body!!.append.div {
            bindText(BindLoc(listOf("test"), "obj", "attr"))
            bindAttributes(mapOf(
                "data-test" to BindLoc(listOf("test"), "obj","attr")
            ))
        }
        JBind.bind(document.body!!, TestProvider)

        assertEquals(TestProvider.INITIAL, testDiv.innerText)
        assertEquals(TestProvider.INITIAL, testDiv.dataset["test"])
        val second = "second"
        TestProvider.testFlow.emit(second)
        assertEquals(second, testDiv.innerText)
        assertEquals(second, testDiv.dataset["test"])
    }
}