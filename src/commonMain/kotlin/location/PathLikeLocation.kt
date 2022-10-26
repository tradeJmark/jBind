package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
sealed interface PathLikeLocation: Location {
    fun sub(subpath: String): Path = Path(subpath, this)
    fun obj(objectName: String): ObjectLocation = ObjectLocation(this, objectName)
}