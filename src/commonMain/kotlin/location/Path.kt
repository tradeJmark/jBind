package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
data class Path internal constructor(val pathName: String? = null, val parent: PathLikeLocation? = null): PathLikeLocation {
    override val length: Int? get() = when {
        pathName == null -> 0
        parent == null -> 1
        else -> parent.length?.plus(1)
    }
    constructor(pathName: String? = null): this(pathName, null)

    override fun sub(subpath: String): Path = if (pathName == null) Path(subpath) else super.sub(subpath)
    init {
        if (pathName == null) {
            require(parent == null) { "Subpath cannot be empty." }
        }
    }
    override fun toString(): String = if (parent == null) pathName ?: "" else "$parent/$pathName"
}