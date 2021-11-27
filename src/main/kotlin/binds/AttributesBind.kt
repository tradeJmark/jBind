package ca.tradejmark.jbind.binds

import ca.tradejmark.jbind.location.BindLoc
import kotlinx.html.HTMLTag

object AttributesBind {
    const val attrName = "data-jbind-attribute"
    const val datasetName = "jbindAttribute"

    fun HTMLTag.bindAttributes(bindings: Map<String, BindLoc>) {
        for ((attr, loc) in bindings) {
            attributes[attr] = loc.toString()
        }
        attributes[attrName] = bindings.keys.joinToString(",")
    }
}