package ca.tradejmark.jbind

import ca.tradejmark.jbind.dsl.AttributesBind
import ca.tradejmark.jbind.dsl.IsHTML
import ca.tradejmark.jbind.dsl.ContentBind
import ca.tradejmark.jbind.dsl.ScopeBind
import ca.tradejmark.jbind.location.ValueLocation
import ca.tradejmark.jbind.transformation.MarkdownTransformation
import ca.tradejmark.jbind.transformation.MarkdownTransformation.Companion.MARKDOWN_TRANSFORMATION
import ca.tradejmark.jbind.transformation.Transformation
import external.markdown_it.MarkdownVariant
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode
import org.w3c.dom.get

object JBind {
    private val transformations = mutableMapOf<String, Transformation>(
        MARKDOWN_TRANSFORMATION to MarkdownTransformation(MarkdownVariant.COMMONMARK)
    )

    fun bind(root: ParentNode, provider: Provider) = bind(root, provider, "")

    private fun bind(root: ParentNode, provider: Provider, scope: String) {
        var newScope = scope
        if (root is HTMLElement) {
            root.dataset[ScopeBind.datasetName]?.let { newScope = scoped(scope, it) }
            if (root.hasAttribute(ContentBind.attrName)) bindContent(root, provider, newScope)
            if (root.hasAttribute(AttributesBind.attrName)) bindAttributes(root, provider, newScope)
        }
        for (i in 0 until root.childElementCount) {
            bind(root.children[i]!!, provider, newScope)
        }
    }

    private fun scoped(scope: String, path: String): String = when {
        scope == "" -> path
        path.startsWith(":") -> "$scope.${path.drop(1)}"
        else -> path
    }

    fun registerTransformation(name: String, transformation: Transformation) {
        transformations[name] = transformation
    }

    internal fun extractContentData(loc: String, scope: String = ""): Pair<String, Transformation?> {
        val split = loc.split("#")
        if (split.size > 2) throw InvalidLocationError(loc, "Cannot contain multiple '#' characters.")
        val location = scoped(scope, split[0])
        val transformation = split.getOrNull(1)?.let {
            transformations[it] ?: throw InvalidLocationError(loc, "No transformation named $it registered.")
        }
        return location to transformation
    }

    private fun bindContent(element: HTMLElement, provider: Provider, scope: String) {
        val textFlow = element.dataset[ContentBind.datasetName]?.let { loc ->
            val (location, transformation) = extractContentData(loc, scope)
            provider.getValue(ValueLocation(location)).map {
                val htmlByAttr = element.dataset[IsHTML.datasetName].toBoolean()
                if (transformation != null) {
                    val content = transformation.transform(it)
                    ContentData(content, transformation.outputIsHtml or htmlByAttr)
                }
                else ContentData(it, htmlByAttr)
            }
        } ?: return
        JBindScope.launch { textFlow.collect { (content, isHtml) ->
            if (isHtml) element.innerHTML = content
            else element.innerText = content
        } }
    }

    private fun bindAttributes(element: HTMLElement, provider: Provider, scope: String) {
        val attrFlows = element.dataset[AttributesBind.datasetName]
            ?.split(",")
            ?.map {
                it to provider.getValue(ValueLocation(scoped(scope, element.getAttribute(it)!!)))
            } ?: return
        attrFlows.forEach { (attr, valuesFlow) ->
            JBindScope.launch {
                valuesFlow.collect { element.setAttribute(attr, it) }
            }
        }
    }
}