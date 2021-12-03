package ca.tradejmark.jbind

import ca.tradejmark.jbind.BindTests.TestProvider.HTML_INNER_ID
import ca.tradejmark.jbind.BindTests.TestProvider.HTML_INNER_TEXT
import ca.tradejmark.jbind.TestUtils.delayForUpdate
import ca.tradejmark.jbind.dsl.AttributesBind.bindAttributes
import ca.tradejmark.jbind.dsl.ContentBind.bindContent
import ca.tradejmark.jbind.dsl.IsHTML.contentIsHtml
import ca.tradejmark.jbind.location.BindPath
import ca.tradejmark.jbind.location.BindValueLocation
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.dom.clear
import kotlinx.html.id
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class BindTests {
    object TestProvider: Provider {
        const val INITIAL = "init"
        const val INITIAL2 = "2init2"
        const val HTML_INNER_ID = "inner"
        const val HTML_INNER_TEXT = "text"
        const val HTML_CONTENT = "<div id=\"$HTML_INNER_ID\">$HTML_INNER_TEXT</div>"

        val testFlow = MutableStateFlow(INITIAL)
        val testFlow2 = MutableStateFlow(INITIAL2)
        val htmlFlow = MutableStateFlow(HTML_CONTENT)
        override fun getValue(location: BindValueLocation): Flow<String> {
            if (location == BindPath("test").obj("obj").value("attr")) {
                return  testFlow
            }
            if (location == BindPath("test").sub("inner").obj("obj").value("attr")) {
                return testFlow2
            }
            if (location == BindPath("html").obj("test").value("body")) {
                return htmlFlow
            }
            else throw UnavailableError(location)
        }
    }

    @BeforeTest
    fun clearTestFlows() {
        TestProvider.testFlow.tryEmit(TestProvider.INITIAL)
        TestProvider.testFlow2.tryEmit(TestProvider.INITIAL2)
    }

    @BeforeTest
    fun emptyBody() {
        document.body!!.clear()
    }

    @Test
    fun testTextBinding() = runTest {
        val testDiv = document.body!!.append.div {
            bindContent(BindPath("test").obj("obj").value("attr"))
        }
        JBind.bind(document.body!!, TestProvider)

        delayForUpdate()
        assertEquals(TestProvider.INITIAL, testDiv.innerText)
        val new = "new"
        TestProvider.testFlow.emit(new)
        delayForUpdate()
        assertEquals(new, testDiv.innerText)
    }

    @Test
    fun testAttributeBinding() = runTest {
        val testDiv = document.body!!.append.div {
            id = "test-div"
            bindAttributes(mapOf(
                "testA" to BindPath("test").obj("obj").value("attr"),
                "testB" to BindPath("test").sub("inner").obj("obj").value("attr")
            ))
        }
        JBind.bind(document.body!!, TestProvider)

        delayForUpdate()
        assertEquals(TestProvider.INITIAL, testDiv.getAttribute("testA"))
        assertEquals(TestProvider.INITIAL2, testDiv.getAttribute("testB"))
        val second = "second"
        TestProvider.testFlow.emit(second)
        delayForUpdate()
        assertEquals(second, testDiv.getAttribute("testA"))
        assertEquals(TestProvider.INITIAL2, testDiv.getAttribute("testB"))
        val third = "third"
        TestProvider.testFlow.emit(third)
        TestProvider.testFlow2.emit(third)
        delayForUpdate()
        assertEquals(third, testDiv.getAttribute("testA"))
        assertEquals(third, testDiv.getAttribute("testB"))
    }

    @Test
    fun testAllBindings() = runTest {
        val testDiv = document.body!!.append.div {
            bindContent(BindPath("test").obj("obj").value("attr"))
            bindAttributes(mapOf(
                "data-test" to BindPath("test").obj("obj").value("attr")
            ))
        }
        JBind.bind(document.body!!, TestProvider)

        delayForUpdate()
        assertEquals(TestProvider.INITIAL, testDiv.innerText)
        assertEquals(TestProvider.INITIAL, testDiv.dataset["test"])
        val second = "second"
        TestProvider.testFlow.emit(second)
        delayForUpdate()
        assertEquals(second, testDiv.innerText)
        assertEquals(second, testDiv.dataset["test"])
    }

    @Test
    fun testContentIsHtml() = runTest {
        val testDiv = document.body!!.append.div {
            bindContent(BindPath("html").obj("test").value("body"))
            contentIsHtml = true
        }
        JBind.bind(document.body!!, TestProvider)

        delayForUpdate()
        assertEquals(testDiv.innerHTML, TestProvider.HTML_CONTENT)
        assertEquals(HTML_INNER_ID, (testDiv.firstChild as? HTMLElement)?.id)
        assertEquals(HTML_INNER_TEXT, (testDiv.firstChild as? HTMLElement)?.innerText)
    }
}