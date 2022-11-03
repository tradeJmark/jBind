package ca.tradejmark.jbind.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class LocationTests {
    companion object {
        val aPath = Path("This").sub("is").sub("a").sub("path")
        const val aPathString = "This/is/a/path"

        val anObject = aPath.obj("object")
        const val anObjectString = "$aPathString:object"

        val anArrayItem = anObject.arrayItem(15)
        const val anArrayItemString = "$anObjectString[15]"

        val anObjectValue = anObject.value("name")
        const val anObjectValueString = "$anObjectString.name"

        val anArrayItemValue = anArrayItem.value("name")
        const val anArrayItemValueString = "$anArrayItemString.name"

        val aRelativePath = RelativePath.sub("more")
        const val aRelativePathString = "#/more"
    }
    @Test
    fun testPathToString() {
        assertEquals(aPathString, aPath.toString())
        assertEquals("test", Path("test").toString())
    }

    @Test
    fun testPathFromString() {
        assertEquals(aPath, Location.fromString(aPathString))
        assertEquals(Path("test"), Location.fromString("test"))
    }

    @Test
    fun testObjectToString() {
        assertEquals(anObjectString, anObject.toString())
        assertEquals(":test", Path().obj("test").toString())
    }

    @Test
    fun testObjectFromString() {
        assertEquals(anObject, Location.fromString(anObjectString))
        assertEquals(Path().obj("test"), Location.fromString(":test"))
    }

    @Test
    fun testArrayItemToString() {
        assertEquals(anArrayItemString, anArrayItem.toString())
    }

    @Test
    fun testArrayItemFromString() {
        assertEquals(anArrayItem, Location.fromString(anArrayItemString))
    }

    @Test
    fun testValueToString() {
        assertEquals(anObjectValueString, anObjectValue.toString())
        assertEquals(anArrayItemValueString, anArrayItemValue.toString())
    }

    @Test
    fun testValueFromString() {
        assertEquals(anObjectValue, Location.fromString(anObjectValueString))
        assertEquals(anArrayItemValue, Location.fromString(anArrayItemValueString))
    }

    @Test
    fun testRelativePathToString() {
        assertEquals(aRelativePathString, aRelativePath.toString())
    }

    @Test
    fun testRelativePathFromString() {
        val resolved = aRelativePath.copy(parent = aPath)
        assertEquals(resolved, Location.fromString(aRelativePathString, scope = aPath))
    }

    @Test
    fun testBadLocationStrings() {
        assertFails { Location.fromString("/leading/slash") }
        assertFails { Location.fromString("double:array[3][4]") }
        assertFails { Location.fromString("array/on/path[3]") }
        assertFails { Location.fromString("value/on.path") }
        assertFails { Location.fromString("value/on.path") }
        assertFails { Location.fromString("slash:after/object") }
        assertFails { Location.fromString(":slash.after/value") }
        assertFails { Location.fromString("") }
        assertFails { Location.fromString("array:on.value[3]") }
        assertFails { Location.fromString("array:on.value[3]") }
        assertFails { Location.fromString(":value.on.value") }
        assertFails { Location.fromString("/empty//path:segment") }
        assertFails { Location.fromString("old.style.path") }
        assertFails { Location.fromString("#/relative/without:scope") }
        assertFails { Location.fromString("#/too/many:relative#markers", scope = aPath) }
    }
}