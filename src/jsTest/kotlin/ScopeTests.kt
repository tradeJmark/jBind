package ca.tradejmark.jbind

import ca.tradejmark.jbind.dsl.AttributesBind.AttributeValueData
import ca.tradejmark.jbind.dsl.AttributesBind.bindAttributes
import ca.tradejmark.jbind.dsl.ContentBind.bindContent
import ca.tradejmark.jbind.dsl.ScopeBind.setScope
import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.Path
import ca.tradejmark.jbind.location.RelativePath
import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.dom.clear
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class ScopeTests {
    object TestProvider: Provider {
        const val ROOT_VAL = "7"
        const val SUB_VAL = "8"
        override fun getValue(location: ValueLocation): Flow<String> = when (location) {
            Path("root").obj("a").value("val") -> flowOf(ROOT_VAL)
            Path("root").sub("sub").obj("a").value("val") -> flowOf(SUB_VAL)
            else -> throw InvalidLocationError(location.toString(), "Not provided.")
        }

        override fun getArrayLength(location: ObjectLocation): Flow<Int> { throw UnavailableError(location) }
    }

    @BeforeTest
    fun emptyBody() {
        document.body!!.clear()
    }

    @Test
    fun testScoping() = runTest {
        val testDiv = document.body!!.append.div {
            setScope(Path("root"))
            bindAttributes(mapOf("test" to AttributeValueData(RelativePath.obj("a").value("val"))))
            div {
                setScope(RelativePath.sub("sub"))
                bindContent(RelativePath.obj("a").value("val"))
            }
        }
        JBind.bind(document.body!!, TestProvider)

        TestUtils.delayForUpdate()
        assertEquals(TestProvider.ROOT_VAL, testDiv.getAttribute("test"))
        assertEquals(TestProvider.SUB_VAL, (testDiv.firstElementChild as HTMLDivElement).innerText)
    }
}