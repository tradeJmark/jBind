package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
data class ArrayItemLocation internal constructor(val obj: ObjectLocation, val index: Int? = null): ObjectLikeLocation {
    override fun toString(): String = "$obj[${index ?: ""}]"
}