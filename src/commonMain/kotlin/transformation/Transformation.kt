package ca.tradejmark.jbind.transformation

fun interface Transformation {
    fun transform(from: String): String
    val outputIsHtml: Boolean get() = false
}