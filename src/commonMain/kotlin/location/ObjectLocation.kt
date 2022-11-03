package ca.tradejmark.jbind.location

import ca.tradejmark.jbind.InvalidLocationError
import kotlinx.serialization.Serializable

@Serializable
data class ObjectLocation internal constructor(override val path: PathLikeLocation, override val objectName: String): ObjectLikeLocation {
    operator fun get(index: Int): ArrayItemLocation = arrayItem(index)
    fun arrayItem(index: Int): ArrayItemLocation = ArrayItemLocation(this, index)
    override fun toString(): String = "$path:$objectName"

    companion object {
        internal operator fun invoke(fullPath: String): ObjectLocation =
            Location.fromString(fullPath) as? ObjectLocation
                ?: throw InvalidLocationError(fullPath, "Location does not represent an object")
    }
}