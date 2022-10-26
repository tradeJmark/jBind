package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.location.Location
import kotlinx.html.HTMLTag

object ScopeBind {
    const val attrName = "data-jbind-scope"
    const val datasetName = "jbindScope"

    fun HTMLTag.setScope(location: Location) {
        attributes[attrName] = location.toString()
    }
}