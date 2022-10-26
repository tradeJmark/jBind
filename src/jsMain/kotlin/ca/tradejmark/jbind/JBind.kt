package ca.tradejmark.jbind

import ca.tradejmark.jbind.dsl.AttributesBind
import ca.tradejmark.jbind.dsl.IsHTML
import ca.tradejmark.jbind.dsl.ContentBind
import ca.tradejmark.jbind.dsl.ScopeBind
import ca.tradejmark.jbind.location.Location
import ca.tradejmark.jbind.location.Path
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
    object DefaultTransformations {
        val markdown = MarkdownTransformation(MarkdownVariant.COMMONMARK)
    }
    internal val transformations = mutableMapOf<String, Transformation>(
        MARKDOWN_TRANSFORMATION to DefaultTransformations.markdown
    )

    fun bind(root: ParentNode, provider: Provider) = traverse(root, { elem, scope ->
        if (elem.hasAttribute(ContentBind.attrName)) bindContent(elem, provider, scope)
        if (elem.hasAttribute(AttributesBind.attrName)) bindAttributes(elem, provider, scope)
    })

    internal fun traverse(root: ParentNode, operation: (HTMLElement, Location) -> Unit, scope: Location = Path()) {
        var newScope = scope
        if (root is HTMLElement) {
            root.dataset[ScopeBind.datasetName]?.let { newScope = Location.fromString(it, scope) }
            operation(root, newScope)
        }
        for (i in 0 until root.childElementCount) {
            traverse(root.children[i]!!, operation, newScope)
        }
    }

    fun registerTransformation(name: String, transformation: Transformation) {
        transformations[name] = transformation
    }

    internal fun lookupTransformationString(transformation: Transformation): String? {
        return transformations.toList().find { (_, tf) -> tf == transformation }?.first
    }

    private fun bindContent(element: HTMLElement, provider: Provider, scope: Location) {
        val textFlow = element.dataset[ContentBind.datasetName]?.let { loc ->
            val location = Location.fromString(loc, scope) as? ValueLocation
                ?: throw InvalidLocationError(loc, "Does not represent a value")
            val transformation = element.dataset[ContentBind.transformDatasetName]?.let { transformations[it] }
            provider.getValue(location).map {
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

    private fun bindAttributes(element: HTMLElement, provider: Provider, scope: Location) {
        val attrFlows = element.dataset[AttributesBind.datasetName]
            ?.split(";")
            ?.map {
                val attrName = it.substringBefore("=")
                val locStr = it.substringAfter("=")
                val loc = Location.fromString(locStr, scope) as? ValueLocation
                    ?: throw InvalidLocationError(locStr, "Does not represent a value")
                attrName to provider.getValue(loc)
            } ?: return
        attrFlows.forEach { (attr, valuesFlow) ->
            JBindScope.launch {
                valuesFlow.collect { element.setAttribute(attr, it) }
            }
        }
    }
}