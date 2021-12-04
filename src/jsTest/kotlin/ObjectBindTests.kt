package ca.tradejmark.jbind

import ca.tradejmark.jbind.TestUtils.delayForUpdate
import ca.tradejmark.jbind.dsl.ObjectBind
import ca.tradejmark.jbind.dsl.ObjectBind.bindObject
import ca.tradejmark.jbind.dsl.ObjectBind.valueIsContent
import ca.tradejmark.jbind.location.BindObjectLocation
import ca.tradejmark.jbind.location.BindPath
import ca.tradejmark.jbind.location.BindValueLocation
import ca.tradejmark.jbind.serialization.ObjectProvider
import ca.tradejmark.jbind.serialization.bindObjects
import ca.tradejmark.jbind.serialization.decodeFromElement
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalSerializationApi
@ExperimentalCoroutinesApi
class ObjectBindTests {
    @Serializable
    data class TestClass(val a: String, val b: Int)
    object TestProvider: ObjectProvider<TestClass> {
        val testObj = TestClass("testing", 24)

        override fun getValue(location: BindValueLocation): Flow<String> {
            throw UnavailableError(location)
        }

        override fun getObject(location: BindObjectLocation): Flow<TestClass> {
            if (location == BindPath("path").obj("testObj")) {
                return flow { emit(testObj) }
            }
            else throw UnavailableError(location)
        }
    }

    @BeforeTest
    fun clearBody() {
        document.body!!.clear()
    }

    @Test
    fun testObjectBind() = runTest {
        val testDiv = document.body!!.append.div {
            bindObject(BindPath("path").obj("testObj"))
            valueIsContent("a")
        }
        JBind.bindObjects(document.body!!, TestProvider)

        delayForUpdate()
        assertEquals(TestProvider.testObj.a, testDiv.innerText)
        assertEquals(TestProvider.testObj.b.toString(), testDiv.getAttribute(ObjectBind.getValueAttrName("b")))
        val deser = decodeFromElement<TestClass>(testDiv, contentValue = "a")
        assertEquals(TestProvider.testObj, deser)
    }
}