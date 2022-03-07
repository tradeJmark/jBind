package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
data class BindPath(val path: List<String>, val relative: Boolean = false): List<String> by path {
    constructor(path: String): this(path.split("."))
    fun sub(subpath: String): BindPath = BindPath(path + subpath.split("."))
    fun obj(objectName: String): BindObjectLocation = BindObjectLocation(this, objectName)
    override fun toString(): String {
        val relative = if (relative) ":" else ""
        return relative + path.joinToString(".")
    }

    companion object {
        fun relative(path: List<String>): BindPath = BindPath(path, true)
        fun relative(path: String): BindPath = relative(listOf(path))
    }
}