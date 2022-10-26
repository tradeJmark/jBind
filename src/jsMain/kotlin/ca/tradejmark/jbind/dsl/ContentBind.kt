package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.html.HTMLTag

object ContentBind {
    const val attrName = "data-jbind-content"
    const val datasetName = "jbindContent"

    fun HTMLTag.bindContent(location: ValueLocation, transformation: String? = null) {
        val tf = transformation?.let { "#$it" } ?: ""
        attributes[attrName] = location.toString() + tf
    }
}