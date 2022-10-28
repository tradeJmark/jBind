package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
sealed interface ObjectLikeLocation: Location {
    val path: PathLikeLocation
    val objectName: String?
    fun value(valueName: String): ValueLocation = ValueLocation(this, valueName)
}