package ca.tradejmark.jbind

import ca.tradejmark.jbind.dsl.AttributesBind
import ca.tradejmark.jbind.dsl.IsHTML
import ca.tradejmark.jbind.dsl.ContentBind
import ca.tradejmark.jbind.location.BindValueLocation
import ca.tradejmark.jbind.transformation.MarkdownTransformation
import ca.tradejmark.jbind.transformation.MarkdownTransformation.Companion.MARKDOWN_TRANSFORMATION
import ca.tradejmark.jbind.transformation.Transformation
import external.markdown_it.MarkdownVariant
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode
import org.w3c.dom.get

object JBind {
    private val transformations = mutableMapOf<String, Transformation>(
        MARKDOWN_TRANSFORMATION to MarkdownTransformation(MarkdownVariant.COMMONMARK)
    )

    fun bind(root: ParentNode, provider: Provider) {
        val binds = root.querySelectorAll("[${ContentBind.attrName}],[${AttributesBind.attrName}]")
        for (i in 0 until binds.length) {
            val toBind = binds[i] as? HTMLElement ?: continue
            bindContent(toBind, provider)
            bindAttributes(toBind, provider)
        }
    }

    fun registerTransformation(name: String, transformation: Transformation) {
        transformations[name] = transformation
    }

    internal fun extractContentData(loc: String): Pair<String, Transformation?> {
        val split = loc.split("#")
        if (split.size > 2) throw InvalidLocationError(loc, "Cannot contain multiple '#' characters.")
        val location = split[0]
        val transformation = split.getOrNull(1)?.let {
            transformations[it] ?: throw InvalidLocationError(loc, "No transformation named $it registered.")
        }
        return location to transformation
    }

    private fun bindContent(element: HTMLElement, provider: Provider) {
        val textFlow = element.dataset[ContentBind.datasetName]?.let { loc ->
            val (location, transformation) = extractContentData(loc)
            provider.getValue(BindValueLocation(location)).map {
                val htmlByAttr = element.dataset[IsHTML.datasetName].toBoolean()
                if (transformation != null) {
                    val content = transformation.transform(it)
                    ContentData(content, transformation.outputIsHtml or htmlByAttr)
                }
                else ContentData(it, htmlByAttr)
            }
        }
        JBindScope.launch { textFlow?.collect { (content, isHtml) ->
            if (isHtml) element.innerHTML = content
            else element.innerText = content
        } }
    }

    private fun bindAttributes(element: HTMLElement, provider: Provider) {
        val attrFlows = element.dataset[AttributesBind.datasetName]
            ?.split(",")
            ?.map {
                it to provider.getValue(BindValueLocation(element.getAttribute(it)!!))
            }
        attrFlows?.forEach { (attr, valuesFlow) ->
            JBindScope.launch {
                valuesFlow.collect { element.setAttribute(attr, it) }
            }
        }
    }
}