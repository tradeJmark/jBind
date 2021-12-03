package external.markdown_it

@JsModule("markdown-it")
@JsNonModule
external class MarkdownIt() {
    constructor(type: String)
    fun render(md: String): String
}