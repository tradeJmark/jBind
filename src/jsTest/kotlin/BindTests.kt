package ca.tradejmark.jbind

import ca.tradejmark.jbind.BindTests.TestProvider.ARR_ITEMS
import ca.tradejmark.jbind.BindTests.TestProvider.HTML_INNER_ID
import ca.tradejmark.jbind.BindTests.TestProvider.HTML_INNER_TEXT
import ca.tradejmark.jbind.BindTests.TestProvider.INITIAL_ARRAY_LENGTH
import ca.tradejmark.jbind.BindTests.TestProvider.arrayLengthFlow
import ca.tradejmark.jbind.TestUtils.delayForUpdate
import ca.tradejmark.jbind.dsl.AttributesBind.bindAttributes
import ca.tradejmark.jbind.dsl.ContentBind.bindContent
import ca.tradejmark.jbind.dsl.ExpandFromArrayBind
import ca.tradejmark.jbind.dsl.ExpandFromArrayBind.expandFromArray
import ca.tradejmark.jbind.dsl.IsHTML.contentIsHtml
import ca.tradejmark.jbind.location.*
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.dom.clear
import kotlinx.html.id
import org.w3c.dom.HTMLDivElement
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
        const val INITIAL_ARRAY_LENGTH = 3
        val ARR_ITEMS = arrayOf(
            "arr1",
            "arr2",
            "arr3"
        )

        val testFlow = MutableStateFlow(INITIAL)
        val testFlow2 = MutableStateFlow(INITIAL2)
        val htmlFlow = MutableStateFlow(HTML_CONTENT)
        val arrayLengthFlow = MutableStateFlow(INITIAL_ARRAY_LENGTH)
        val arrayFlows = ARR_ITEMS.map { MutableStateFlow(it) }
        override fun getValue(location: ValueLocation): Flow<String> = when (location) {
            Path("test").obj("obj").value("attr") -> testFlow
            Path("test").sub("inner").obj("obj").value("attr") -> testFlow2
            Path("html").obj("test").value("body") -> htmlFlow
            else -> {
                if (location.objectLocation is ArrayItemLocation) {
                    arrayFlows[(location.objectLocation as ArrayItemLocation).index!!]
                }
                else {
                    throw UnavailableError(location)
                }
            }
        }

        override fun getArrayLength(location: ObjectLocation): Flow<Int> = when (location) {
            Path().obj("array") -> arrayLengthFlow
            else -> throw UnavailableError(location)
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
            bindContent(Path("test").obj("obj").value("attr"))
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
                "testA" to Path("test").obj("obj").value("attr"),
                "testB" to Path("test").sub("inner").obj("obj").value("attr")
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
    fun testArrayExpansionBinding() = runTest {
        val testDiv = document.body!!.append.div {
            expandFromArray(Path().obj("array"))
            bindContent(RelativeObjectLocation.allArrayItems().value("doesNotMatter"))
        }
        console.log(document.body)
        JBind.bind(document.body!!, TestProvider)

        delayForUpdate()
        console.log(document.body)
        val div2 = (testDiv.nextElementSibling as? HTMLDivElement)
        val div3 = (div2?.nextElementSibling as? HTMLDivElement)
        assertEquals(ARR_ITEMS[0], testDiv.innerText)
        assertEquals(ARR_ITEMS[1], div2?.innerText)
        assertEquals(ARR_ITEMS[2], div3?.innerText)
        assertEquals(INITIAL_ARRAY_LENGTH, document.body!!.children.length)
        arrayLengthFlow.emit(2)
        delayForUpdate()
        assertEquals(INITIAL_ARRAY_LENGTH - 1, document.body!!.children.length)
        assertEquals(ARR_ITEMS[0], testDiv.innerText)
        assertEquals(ARR_ITEMS[1], div2?.innerText)
    }

    @Test
    fun testAllBindings() = runTest {
        val testDiv = document.body!!.append.div {
            bindContent(Path("test").obj("obj").value("attr"))
            bindAttributes(mapOf(
                "data-test" to Path("test").obj("obj").value("attr")
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
            bindContent(Path("html").obj("test").value("body"))
            contentIsHtml = true
        }
        JBind.bind(document.body!!, TestProvider)

        delayForUpdate()
        assertEquals(testDiv.innerHTML, TestProvider.HTML_CONTENT)
        assertEquals(HTML_INNER_ID, (testDiv.firstChild as? HTMLElement)?.id)
        assertEquals(HTML_INNER_TEXT, (testDiv.firstChild as? HTMLElement)?.innerText)
    }
}