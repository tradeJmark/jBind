package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
sealed class Relative {
    override fun toString(): String = "#"
}

@Serializable
object RelativePath: PathLikeLocation, Relative()

@Serializable
object RelativeObjectLocation: ObjectLikeLocation, Relative()

@Serializable
object RelativeArrayItemLocation: ObjectLikeLocation, Relative()