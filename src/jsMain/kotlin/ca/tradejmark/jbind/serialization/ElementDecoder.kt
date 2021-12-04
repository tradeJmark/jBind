package ca.tradejmark.jbind.serialization

import ca.tradejmark.jbind.dsl.IsHTML
import ca.tradejmark.jbind.dsl.ObjectBind
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

@ExperimentalSerializationApi
internal class ElementDecoder(
    private val element: HTMLElement,
    private val contentValue: String? = null,
    override val serializersModule: SerializersModule = EmptySerializersModule
): Decoder, CompositeDecoder {
    private var index = 0

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        decodeStringElement(descriptor, index).toBoolean()

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
    decodeStringElement(descriptor, index).toByte()

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        decodeStringElement(descriptor, index)[0]

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        decodeStringElement(descriptor, index).toDouble()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (index == descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
        return index++
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        decodeStringElement(descriptor, index).toFloat()

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = this

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        decodeStringElement(descriptor, index).toInt()

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        decodeStringElement(descriptor, index).toLong()

    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        return try {
            decodeSerializableElement(descriptor, index, deserializer, previousValue)
        }
        catch (e: JBindSerializationException) {
            null
        }
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val ser = decodeStringElement(descriptor, index)
        return Json.decodeFromString(deserializer, ser)
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        decodeStringElement(descriptor, index).toShort()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        val valueName = descriptor.getElementName(index)
        return if (valueName == contentValue) {
            if (element.dataset[IsHTML.datasetName].toBoolean()) {
                element.innerHTML
            } else element.innerText
        }
        else {
            val datasetName = ObjectBind.getValueDatasetName(valueName)
            element.dataset[datasetName]
                ?: throw JBindSerializationException(
                    "Attribute '${ObjectBind.getValueAttrName(valueName)}' not available."
                )
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {}

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = this

    override fun decodeBoolean(): Boolean = throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeByte(): Byte = throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeChar(): Char = throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeDouble(): Double = throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
        throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeFloat(): Float = throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this

    override fun decodeInt(): Int = throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeLong(): Long = throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeNotNullMark(): Boolean {
        return element.hasAttribute(ObjectBind.attrName)
    }

    override fun decodeNull(): Nothing? = null

    override fun decodeShort(): Short = throw NotImplementedError("Cannot decode bare primitives.")

    override fun decodeString(): String = throw NotImplementedError("Cannot decode bare primitives.")
}