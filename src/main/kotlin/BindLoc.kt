package ca.tradejmark.jbind

data class BindLoc(val path: List<String>, val obj: String, val attr: String) {
    override fun toString(): String {
        return (path + listOf(obj, attr)).joinToString(".")
    }
    companion object {
        fun parse(loc: String): BindLoc = loc.split(".").let { components ->
            val attr = components.last()
            val obj = components[components.size - 2]
            val path = components.dropLast(2)
            BindLoc(path, obj, attr)
        }
    }
}
