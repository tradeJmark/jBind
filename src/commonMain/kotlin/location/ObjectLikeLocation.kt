package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
sealed interface ObjectLikeLocation: Location {
    fun value(valueName: String): ValueLocation = ValueLocation(this, valueName)
}