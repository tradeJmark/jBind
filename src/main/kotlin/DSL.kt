package ca.tradejmark.jbind

import kotlinx.html.HTMLTag

object DSL {
    fun HTMLTag.bindToItem(location: String) {
        attributes[itemAttrName] = location
    }
}