package ca.tradejmark.jbind.location

data class BindPath(val path: List<String>): List<String> by path {
    constructor(path: String): this(path.split("."))
    fun sub(subpath: String): BindPath = BindPath(path + subpath.split("."))
    fun obj(objectName: String): BindObjectLocation = BindObjectLocation(this, objectName)
    override fun toString(): String {
        return path.joinToString(".")
    }
}