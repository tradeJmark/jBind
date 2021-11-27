package ca.tradejmark.jbind.binds

import ca.tradejmark.jbind.location.BindValueLocation
import kotlinx.html.HTMLTag

object TextBind {
    const val attrName = "data-jbind-text"
    const val datasetName = "jbindText"

    fun HTMLTag.bindText(location: BindValueLocation) {
        attributes[attrName] = location.toString()
    }
}