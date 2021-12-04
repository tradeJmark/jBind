package ca.tradejmark.jbind.dsl

import kotlinx.html.HTMLTag

object IsHTML {
    const val attrName = "data-jbind-content-is-html"
    const val datasetName = "jbindContentIsHtml"

    var HTMLTag.contentIsHtml: Boolean
        get() = attributes[attrName].toBoolean()
        set(value) {
            attributes[attrName] = value.toString()
        }
}