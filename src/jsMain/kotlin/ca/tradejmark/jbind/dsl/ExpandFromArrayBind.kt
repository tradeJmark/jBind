package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.location.ObjectLocation
import kotlinx.html.HTMLTag

object ExpandFromArrayBind {
    const val attrName = "data-jbind-expand-from-array"
    const val datasetName = "jbindExpandFromArray"
    const val expandedAttrName = "data-jbind-expanded"
    const val expandedDatasetName = "jbindExpanded"

    fun HTMLTag.expandFromArray(location: ObjectLocation) {
        attributes[attrName] = location.toString()
    }
}