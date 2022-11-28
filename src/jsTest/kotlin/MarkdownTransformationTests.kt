package ca.tradejmark.jbind

import ca.tradejmark.jbind.JBind.bind
import ca.tradejmark.jbind.MarkdownTransformationTests.TestProvider.MARKDOWN_H1
import ca.tradejmark.jbind.MarkdownTransformationTests.TestProvider.MARKDOWN_LI_1
import ca.tradejmark.jbind.MarkdownTransformationTests.TestProvider.MARKDOWN_LI_2
import ca.tradejmark.jbind.TestUtils.delayForUpdate
import ca.tradejmark.jbind.dsl.ContentBind.bindContent
import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLHeadingElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.get
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class MarkdownTransformationTests {
    object TestProvider: Provider {
        const val MARKDOWN_H1 = "Test"
        const val MARKDOWN_LI_1 = "A"
        const val MARKDOWN_LI_2 = "B"
        const val MARKDOWN = "# $MARKDOWN_H1\n- $MARKDOWN_LI_1\n- $MARKDOWN_LI_2"
        override fun getValue(location: ValueLocation): StateFlow<String> {
            return MutableStateFlow(MARKDOWN)
        }

        override fun getArrayLength(location: ObjectLocation): StateFlow<Int> { throw UnavailableError(location) }
    }

    @BeforeTest
    fun clearBody() {
        document.body!!.clear()
    }

    @Test
    fun testMarkdownTransformation() = runTest {
        val testDiv = document.body!!.append.div {
            bindContent(ValueLocation("where:ev.er"), JBind.DefaultTransformations.markdown)
        }
        bind(document.body!!, TestProvider)

        delayForUpdate()
        val header = testDiv.getElementsByTagName("h1")[0] as? HTMLHeadingElement
        assertEquals(MARKDOWN_H1, header?.innerText)
        val lis = testDiv.getElementsByTagName("li")
        val li1 = lis[0] as? HTMLLIElement
        assertEquals(MARKDOWN_LI_1, li1?.innerText)
        val li2 = lis[1] as? HTMLLIElement
        assertEquals(MARKDOWN_LI_2, li2?.innerText)
        coroutineContext.cancelChildren()
    }
}