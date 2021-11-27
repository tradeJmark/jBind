package ca.tradejmark.jbind

import kotlinx.html.HTMLTag

object TextBind {
    const val attrName = "data-jbind-text"
    const val datasetName = "jbindText"

    fun HTMLTag.bindText(location: BindLoc) {
        attributes[attrName] = location.toString()
    }
}