package ca.tradejmark.jbind.location

data class BindObjectLocation internal constructor(val path: BindPath, val objectName: String) {
    fun value(valueName: String): BindValueLocation = BindValueLocation(this, valueName)
    override fun toString(): String {
        return "$path.$objectName"
    }

    companion object {
        operator fun invoke(wholePath: List<String>): BindObjectLocation {
            require(wholePath.isNotEmpty()) { "Cannot construct object location from empty path." }
            return BindPath(wholePath.dropLast(1)).obj(wholePath.last())
        }
        operator fun invoke(wholePath: String): BindObjectLocation {
            return BindObjectLocation(wholePath.split("."))
        }
    }
}