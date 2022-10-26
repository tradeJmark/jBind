package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
data class ObjectLocation internal constructor(val path: PathLikeLocation, val objectName: String): ObjectLikeLocation {
    fun arrayItem(index: Int): ArrayItemLocation = ArrayItemLocation(this, index)
    override fun toString(): String = "$path:$objectName"

    companion object {
        internal operator fun invoke(fullPath: String): ObjectLocation =
            Location.fromString(fullPath) as? ObjectLocation
                ?: throw IllegalArgumentException("Location does not represent an object")
    }
}