package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.location.BindObjectLocation
import kotlinx.html.HTMLTag
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
object ObjectBind {
    fun getValueAttrName(valueName: String): String = "data-jhl-ser-$valueName"
    fun getValueDatasetName(valueName: String): String = "jhlSer${valueName[0].uppercase()}${valueName.drop(1)}"
    val attrName = "data-jhl-bind-object"
    val datasetName = "jhlBindObject"
    val contentValueAttrName = "data-jbind-value-is-content"
    val contentValueDatasetName = "jbindValueIsContent"

    fun HTMLTag.bindObject(location: BindObjectLocation) {
        attributes[attrName] = location.toString()
    }

    fun HTMLTag.valueIsContent(valueName: String) {
        attributes[contentValueAttrName] = valueName
    }
}