package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.html.HTMLTag

object AttributesBind {
    data class AttributeValueData(val location: ValueLocation, val prefix: String = "", val postfix: String = "")
    const val attrName = "data-jbind-attribute"
    const val datasetName = "jbindAttribute"

    fun HTMLTag.bindAttributes(bindings: Map<String, AttributeValueData>) {
        attributes[attrName] = bindings.toList().joinToString(";") { (attr, d) ->
            val (loc, prefix, postfix) = d
            "$attr=$prefix{$loc}$postfix"
        }
    }
}