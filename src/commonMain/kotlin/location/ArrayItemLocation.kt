package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
data class ArrayItemLocation internal constructor(
    val obj: ObjectLikeLocation,
    val index: Int
): ObjectLikeLocation {
    override val path: PathLikeLocation get() = obj.path
    override val objectName: String? get() = obj.objectName
    override fun toString(): String = "$obj[${index ?: ""}]"
}