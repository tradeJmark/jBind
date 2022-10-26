package ca.tradejmark.jbind.location

import ca.tradejmark.jbind.InvalidLocationError
import kotlinx.serialization.Serializable

@Serializable
data class ValueLocation internal constructor(val objectLocation: ObjectLikeLocation?, val valueName: String): Location {
    override fun toString(): String = "$objectLocation.$valueName"

    companion object {
        internal operator fun invoke(fullPath: String): ValueLocation =
            Location.fromString(fullPath) as? ValueLocation
                ?: throw InvalidLocationError(fullPath, "Location does not represent a value")
    }
}
