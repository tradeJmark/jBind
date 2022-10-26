package ca.tradejmark.jbind.location

sealed interface PathLikeLocation: Location {
    fun sub(subpath: String): Path = Path(subpath, this)
    fun obj(objectName: String): ObjectLocation = ObjectLocation(this, objectName)
}