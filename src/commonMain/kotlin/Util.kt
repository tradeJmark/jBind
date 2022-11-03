package ca.tradejmark.jbind

object Util {
    fun String.substringBetween(start: String, end: String): String {
        val tail = substringAfter(start)
        return tail.substringBefore(end)
    }
}