package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
sealed class Relative {
    override fun toString(): String = "#"
}

@Serializable
object RelativePath: PathLikeLocation, Relative()

@Serializable
object RelativeObjectLocation: ObjectLikeLocation, Relative() {
    operator fun get(index: Int): ArrayItemLocation = arrayItem(index)
    fun arrayItem(index: Int): ArrayItemLocation = ArrayItemLocation(this, index)
    fun allArrayItems(): ArrayItemLocation = ArrayItemLocation(this)
}

@Serializable
object RelativeArrayItemLocation: ObjectLikeLocation, Relative()