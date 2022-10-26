package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.html.HTMLTag

object AttributesBind {
    const val attrName = "data-jbind-attribute"
    const val datasetName = "jbindAttribute"

    fun HTMLTag.bindAttributes(bindings: Map<String, ValueLocation>) {
        for ((attr, loc) in bindings) {
            attributes[attr] = loc.toString()
        }
        attributes[attrName] = bindings.keys.joinToString(",")
    }
}