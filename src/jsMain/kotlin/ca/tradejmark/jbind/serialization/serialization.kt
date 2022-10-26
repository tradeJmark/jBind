package ca.tradejmark.jbind.serialization

import ca.tradejmark.jbind.InvalidLocationError
import ca.tradejmark.jbind.JBind
import ca.tradejmark.jbind.JBindScope
import ca.tradejmark.jbind.dsl.ObjectBind
import ca.tradejmark.jbind.location.Location
import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.transformation.Transformation
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode
import org.w3c.dom.get

@OptIn(ExperimentalSerializationApi::class)
fun <T> JBind.bindObjects(
    root: ParentNode,
    provider: ObjectProvider<T>,
    serializer: SerializationStrategy<T>) = traverse(root, { elem, scope ->
    val location = elem.dataset[ObjectBind.datasetName]
        ?.let { Location.fromString(it, scope) as? ObjectLocation ?: throw InvalidLocationError(it, "Does not represent an object") }
        ?: return@traverse
    JBindScope.launch {
        provider.getObject(location).collect { obj ->
            val contentValue = elem.dataset[ObjectBind.contentValueDatasetName]
            val transformation = elem.dataset[ObjectBind.contentTransformationDatasetName]?.let { transformations[it] }
            encodeToElement(obj, elem, contentValue = contentValue, transformation = transformation, serializer)
        }
    }
})

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

@OptIn(ExperimentalSerializationApi::class)
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

inline fun <reified T> decodeFromElement(
    element: HTMLElement,
    contentValue: String? = null
): T {
    return decodeFromElement(element, contentValue, serializer())
}