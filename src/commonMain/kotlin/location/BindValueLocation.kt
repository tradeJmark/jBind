package ca.tradejmark.jbind.location

import kotlinx.serialization.Serializable

@Serializable
data class BindValueLocation internal constructor(val objectLocation: BindObjectLocation?, val valueName: String) {
    override fun toString(): String {
        val obj = objectLocation?.let { "$it." } ?: ":"
        return "$obj$valueName"
    }
    companion object {
        operator fun invoke(wholePath: List<String>): BindValueLocation {
            require(wholePath.size >= 2) {
                "Value location's path must at minimum contain an object name and a value name."
            }
            val path = BindPath(wholePath.dropLast(2))
            val objectLocation = path.obj(wholePath[wholePath.size - 2])
            return objectLocation.value(wholePath.last())
        }
        operator fun invoke(wholePath: String): BindValueLocation {
            return BindValueLocation(wholePath.split("."))
        }

        fun relative(valueName: String): BindValueLocation = BindValueLocation(null, valueName)
    }
}
