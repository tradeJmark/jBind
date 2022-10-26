package ca.tradejmark.jbind.location

sealed interface ObjectLikeLocation: Location {
    fun value(valueName: String): ValueLocation = ValueLocation(this, valueName)
}