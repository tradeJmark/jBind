package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.JBind
import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.transformation.Transformation
import kotlinx.html.HTMLTag

object ObjectBind {
    fun getValueAttrName(valueName: String): String = "data-jbind-ser-$valueName"
    fun getValueDatasetName(valueName: String): String = "jbindSer${valueName[0].uppercase()}${valueName.drop(1)}"
    const val attrName = "data-jbind-object"
    const val datasetName = "jbindObject"
    const val contentValueAttrName = "data-jbind-value-is-content"
    const val contentValueDatasetName = "jbindValueIsContent"
    const val contentTransformationAttrName = "data-jbind-object-transformation"
    const val contentTransformationDatasetName = "jbindObjectTransformation"

    fun HTMLTag.bindObject(location: ObjectLocation) {
        attributes[attrName] = location.toString()
    }

    fun HTMLTag.valueIsContent(valueName: String, transformation: Transformation? = null) {
        attributes[contentValueAttrName] = valueName
        transformation?.let {
            val tfString = JBind.lookupTransformationString(it)
            require(tfString != null) { "Provided transformation is not registered" }
            attributes[contentTransformationAttrName] =  tfString
        }
    }
}