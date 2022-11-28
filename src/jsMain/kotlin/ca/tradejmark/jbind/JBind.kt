package ca.tradejmark.jbind

import ca.tradejmark.jbind.Util.substringBetween
import ca.tradejmark.jbind.dsl.*
import ca.tradejmark.jbind.location.Location
import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.Path
import ca.tradejmark.jbind.location.ValueLocation
import ca.tradejmark.jbind.transformation.MarkdownTransformation
import ca.tradejmark.jbind.transformation.MarkdownTransformation.Companion.MARKDOWN_TRANSFORMATION
import ca.tradejmark.jbind.transformation.Transformation
import external.markdown_it.MarkdownVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode
import org.w3c.dom.get
import org.w3c.dom.set

object JBind {
    object DefaultTransformations {
        val markdown = MarkdownTransformation(MarkdownVariant.COMMONMARK)
    }
    internal val transformations = mutableMapOf<String, Transformation>(
        MARKDOWN_TRANSFORMATION to DefaultTransformations.markdown
    )

    fun CoroutineScope.bind(root: ParentNode, provider: Provider) = traverse(root, provider, { elem, scope ->
        if (elem.hasAttribute(ContentBind.attrName)) bindContent(elem, provider, scope)
        if (elem.hasAttribute(AttributesBind.attrName)) bindAttributes(elem, provider, scope)
    })

    internal fun CoroutineScope.traverse(root: ParentNode, provider: Provider, operation: CoroutineScope.(HTMLElement, Location) -> Unit, scope: Location = Path()) {
        var newScope = scope
        if (root is HTMLElement) {
            root.dataset[ScopeBind.datasetName]?.let { newScope = Location.fromString(it, scope) }
            root.dataset[ExpandFromArrayBind.datasetName]?.let { arrLocStr ->
                val arrLoc = Location.fromString(arrLocStr, newScope) as? ObjectLocation
                    ?: throw InvalidLocationError(arrLocStr, "Location does not represent an array")
                root.attributes.removeNamedItem(ExpandFromArrayBind.attrName)
                val clone = root.cloneNode(true)
                root.style.display = "none"
                launch {
                    provider.getArrayLength(arrLoc).filterNotNull().collect { length ->
                        coroutineContext.cancelChildren()
                        var current = root.nextSibling
                        while (current is HTMLElement && current.dataset[ExpandFromArrayBind.expandedDatasetName] == true.toString()) {
                            val newCurrent = current.nextSibling
                            current.remove()
                            current = newCurrent
                        }
                        for (i in length - 1 downTo 0) {
                            val indScope = arrLoc[i]
                            val new = clone.cloneNode(true) as HTMLElement
                            new.dataset[ExpandFromArrayBind.expandedDatasetName] = true.toString()
                            root.after(new)
                            traverse(new, provider, operation, indScope)
                        }
                    }
                }
                newScope = arrLoc[0]
            } ?: operation(root, newScope)
        }
        for (i in 0 until root.childElementCount) {
            traverse(root.children[i]!!, provider, operation, newScope)
        }
    }

    fun registerTransformation(name: String, transformation: Transformation) {
        transformations[name] = transformation
    }

    internal fun lookupTransformationString(transformation: Transformation): String? {
        return transformations.toList().find { (_, tf) -> tf == transformation }?.first
    }

    private fun CoroutineScope.bindContent(element: HTMLElement, provider: Provider, scope: Location) {
        val textFlow = element.dataset[ContentBind.datasetName]?.let { loc ->
            val location = Location.fromString(loc, scope) as? ValueLocation
                ?: throw InvalidLocationError(loc, "Does not represent a value")
            val transformation = element.dataset[ContentBind.transformDatasetName]?.let { transformations[it] }
            provider.getValue(location).filterNotNull().map {
                val htmlByAttr = element.dataset[IsHTML.datasetName].toBoolean()
                if (transformation != null) {
                    val content = transformation.transform(it)
                    ContentData(content, transformation.outputIsHtml or htmlByAttr)
                }
                else ContentData(it, htmlByAttr)
            }
        } ?: return
        launch { textFlow.collect { (content, isHtml) ->
            if (isHtml) element.innerHTML = content
            else element.innerText = content
        }}
    }

    private fun CoroutineScope.bindAttributes(element: HTMLElement, provider: Provider, scope: Location) {
        data class AttrData(
            val attrName: String,
            val preStr: String,
            val postString: String,
            val values: Flow<String>
        )
        val attrFlows = element.dataset[AttributesBind.datasetName]
            ?.split(";")
            ?.map {
                val attrName = it.substringBefore("=")
                val valStr = it.substringAfter("=")
                val preStr = valStr.substringBefore("{")
                val postStr = valStr.substringAfter("}")
                val locStr = valStr.substringBetween("{", "}")
                val loc = Location.fromString(locStr, scope) as? ValueLocation
                    ?: throw InvalidLocationError(locStr, "Does not represent a value")
                AttrData(attrName, preStr, postStr, provider.getValue(loc).filterNotNull())
            } ?: return
        attrFlows.forEach { (attr, preStr, postStr, valuesFlow) ->
            launch {
                valuesFlow.collect {
                    element.setAttribute(attr, "$preStr$it$postStr")
                }
            }
        }
    }
}