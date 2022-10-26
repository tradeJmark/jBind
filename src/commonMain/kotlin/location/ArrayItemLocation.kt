package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
data class ArrayItemLocation internal constructor(val obj: ObjectLocation, val index: Int): ObjectLikeLocation {
    override fun toString(): String = "$obj[$index]"
}