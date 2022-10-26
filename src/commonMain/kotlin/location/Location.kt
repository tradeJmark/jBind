package ca.tradejmark.jbind.location

sealed interface Location {
    companion object {
        //IntelliJ warns the backslash on the ] is redundant, and that's true on JVM, but it's needed on JS.
        private val regex = Regex("""(?<path>\w+(/\w+)*)?(:(?<obj>\w+)(\[(?<arrInd>\d)\])?\.?(?<value>\w+)?)?""")

        fun fromString(loc: String, scope: Location? = null): Location {
            require(loc != "") { "Empty string does not represent a valid location" }
            if (loc.startsWith("#")) {
                require(scope != null) { "Cannot parse relative location without scope" }
                return fromString(loc.replaceFirst("#", scope.toString()))
            }
            require(!loc.contains("#")) { "Location string is not valid" }
            val match = regex.matchEntire(loc)
            require(match != null) { "Location string is not valid" }
            val groups = match.groups as MatchNamedGroupCollection
            val path = groups["path"]?.value?.let {
                it.split("/").fold(Path()) { p, s -> p.sub(s) }
            } ?: Path()
            val obj = groups["obj"]?.value?.let { path.obj(it) }
            val arr = groups["arrInd"]?.value?.toInt()?.let { obj?.arrayItem(it) }
            val value = groups["value"]?.value?.let { arr?.value(it) ?: obj?.value(it) }
            return value ?: arr ?: obj ?: path
        }
    }
}