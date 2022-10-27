package ca.tradejmark.jbind.location

import ca.tradejmark.jbind.InvalidLocationError
import kotlinx.serialization.Serializable

@Serializable
data class ObjectLocation internal constructor(val path: PathLikeLocation, val objectName: String): ObjectLikeLocation {
    operator fun get(index: Int): ArrayItemLocation = arrayItem(index)
    fun arrayItem(index: Int): ArrayItemLocation = ArrayItemLocation(this, index)
    fun allArrayItems(): ArrayItemLocation = ArrayItemLocation(this)
    override fun toString(): String = "$path:$objectName"

    companion object {
        internal operator fun invoke(fullPath: String): ObjectLocation =
            Location.fromString(fullPath) as? ObjectLocation
                ?: throw InvalidLocationError(fullPath, "Location does not represent an object")
    }
}