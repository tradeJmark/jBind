package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.Path
import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.html.HTMLTag

object ScopeBind {
    const val attrName = "data-jbind-scope"
    const val datasetName = "jbindScope"

    fun HTMLTag.setScope(location: ValueLocation) {
        attributes[attrName] = location.toString()
    }
    fun HTMLTag.setScope(location: ObjectLocation) {
        attributes[attrName] = location.toString()
    }
    fun HTMLTag.setScope(location: Path) {
        attributes[attrName] = location.toString()
    }
}