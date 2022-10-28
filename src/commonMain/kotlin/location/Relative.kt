package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
sealed class Relative {
    override fun toString(): String = "#"
}

@Serializable
object RelativePath: PathLikeLocation, Relative() {
    override val length: Int? get() = null
}

@Serializable
object RelativeObjectLocation: ObjectLikeLocation, Relative() {
    override val objectName: String? get() = null
    override val path: PathLikeLocation get() = RelativePath
    operator fun get(index: Int): ArrayItemLocation = arrayItem(index)
    fun arrayItem(index: Int): ArrayItemLocation = ArrayItemLocation(this, index)
    fun allArrayItems(): ArrayItemLocation = ArrayItemLocation(this)
}

@Serializable
object RelativeArrayItemLocation: ObjectLikeLocation, Relative() {
    override val objectName: String? get() = null
    override val path: PathLikeLocation get() = RelativePath
}