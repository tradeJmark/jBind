package ca.tradejmark.jbind.serialization

import ca.tradejmark.jbind.dsl.IsHTML
import ca.tradejmark.jbind.dsl.ObjectBind
import ca.tradejmark.jbind.transformation.Transformation
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.w3c.dom.HTMLElement
import org.w3c.dom.get
import org.w3c.dom.set

@ExperimentalSerializationApi
internal class ElementEncoder(
    private val element: HTMLElement,
    private val contentValue: String? = null,
    private val contentTransformation: Transformation? = null,
    override val serializersModule: SerializersModule = EmptySerializersModule
): Encoder, CompositeEncoder {
    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this
    override fun encodeBoolean(value: Boolean) = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeByte(value: Byte) = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeChar(value: Char) = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeDouble(value: Double) = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) =
        throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeFloat(value: Float) = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this

    override fun encodeInt(value: Int) = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeLong(value: Long) = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeNull() = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeShort(value: Short) = throw NotImplementedError("Cannot encode bare primitives")

    override fun encodeString(value: String)  = throw NotImplementedError("Cannot encode bare primitives")

    private fun <T> encodeElementValue(descriptor: SerialDescriptor, index: Int, value: T) {
        val valueName = descriptor.getElementName(index)
        if (valueName == contentValue) {
            val tVal = contentTransformation?.transform(value.toString()) ?: value.toString()
            if (contentTransformation?.outputIsHtml == true || element.dataset[IsHTML.datasetName].toBoolean()) {
                element.innerHTML = tVal
            }
            else {
                element.innerText = tVal
            }
        }
        else {
            val datasetName = ObjectBind.getValueDatasetName(valueName)
            element.dataset[datasetName] = value.toString()
        }
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) =
        encodeElementValue(descriptor, index, value)

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) =
        encodeElementValue(descriptor, index, value)

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) =
        encodeElementValue(descriptor, index, value)

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) =
        encodeElementValue(descriptor, index, value)

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) =
        encodeElementValue(descriptor, index, value)

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder = this

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) =
        encodeElementValue(descriptor, index, value)

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) =
        encodeElementValue(descriptor, index, value)

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        if (value != null) encodeSerializableElement(descriptor, index, serializer, value)
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val ser = Json.encodeToString(serializer, value)
        encodeStringElement(descriptor, index, ser)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) =
        encodeElementValue(descriptor, index, value)

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) =
        encodeElementValue(descriptor, index, value)

    override fun endStructure(descriptor: SerialDescriptor) {}
}