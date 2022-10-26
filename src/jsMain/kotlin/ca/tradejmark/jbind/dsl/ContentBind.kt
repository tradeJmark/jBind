package ca.tradejmark.jbind.dsl

import ca.tradejmark.jbind.JBind
import ca.tradejmark.jbind.location.ValueLocation
import ca.tradejmark.jbind.transformation.Transformation
import kotlinx.html.HTMLTag

object ContentBind {
    const val attrName = "data-jbind-content"
    const val datasetName = "jbindContent"
    const val transformAttrName = "data-jbind-content-transformation"
    const val transformDatasetName = "jbindContentTransformation"

    fun HTMLTag.bindContent(location: ValueLocation, transformation: Transformation? = null) {
        attributes[attrName] = location.toString()
        transformation?.let {
            val tfString = JBind.lookupTransformationString(transformation)
            require(tfString != null) { "Provided transformation is not registered" }
            attributes[transformAttrName] = tfString
        }
    }
}