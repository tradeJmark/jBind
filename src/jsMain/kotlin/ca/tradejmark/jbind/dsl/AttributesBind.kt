package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.html.HTMLTag

object AttributesBind {
    const val attrName = "data-jbind-attribute"
    const val datasetName = "jbindAttribute"

    fun HTMLTag.bindAttributes(bindings: Map<String, ValueLocation>) {
        attributes[attrName] = bindings.toList().joinToString(";") { (attr, loc) -> "$attr=$loc" }
    }
}