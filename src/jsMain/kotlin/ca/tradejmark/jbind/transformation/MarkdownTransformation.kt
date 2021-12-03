package ca.tradejmark.jbind.transformation

import external.markdown_it.MarkdownIt
import external.markdown_it.MarkdownVariant

class MarkdownTransformation(private val type: MarkdownVariant): Transformation {
    private val md by lazy { MarkdownIt(type.string) }
    override fun transform(from: String): String = md.render(from)
    override val outputIsHtml: Boolean get() = true

    companion object {
        const val MARKDOWN_TRANSFORMATION = "markdown"
    }
}