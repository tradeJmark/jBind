package ca.tradejmark.jbind.serialization

import ca.tradejmark.jbind.JBind
import ca.tradejmark.jbind.JBindScope
import ca.tradejmark.jbind.dsl.ObjectBind
import ca.tradejmark.jbind.location.BindObjectLocation
import ca.tradejmark.jbind.transformation.Transformation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode
import org.w3c.dom.get

@ExperimentalSerializationApi
fun <T> JBind.bindObjects(root: ParentNode, provider: ObjectProvider<T>, serializer: SerializationStrategy<T>) {
    val binds = root.querySelectorAll("[${ObjectBind.attrName}]")
    for (i in 0 until binds.length) {
        val toBind = binds[i] as? HTMLElement ?: continue
        val (location, transformation) = extractContentData(toBind.dataset[ObjectBind.datasetName]!!)
        JBindScope.launch {
            provider.getObject(BindObjectLocation(location)).collect {
                val contentValue = toBind.dataset[ObjectBind.contentValueDatasetName]
                encodeToElement(it, toBind, contentValue = contentValue, transformation = transformation, serializer)
            }
        }
    }
}

@ExperimentalSerializationApi
inline fun <reified T> JBind.bindObjects(root: ParentNode, provider: ObjectProvider<T>) {
    bindObjects(root, provider, serializer())
}

@ExperimentalSerializationApi
internal fun <T> encodeToElement(
    value: T,
    element: HTMLElement,
    contentValue: String? = null,
    transformation: Transformation? = null,
    serializer: SerializationStrategy<T>
) {
    ElementEncoder(
        element,
        contentValue = contentValue,
        contentTransformation = transformation
    ).encodeSerializableValue(serializer, value)
}

@ExperimentalSerializationApi
fun <T> decodeFromElement(
    element: HTMLElement,
    contentValue: String? = null,
    serializer: DeserializationStrategy<T>
): T {
    return ElementDecoder(
        element,
        contentValue = contentValue,
    ).decodeSerializableValue(serializer)
}

@ExperimentalSerializationApi
inline fun <reified T> decodeFromElement(
    element: HTMLElement,
    contentValue: String? = null
): T {
    return decodeFromElement(element, contentValue, serializer())
}