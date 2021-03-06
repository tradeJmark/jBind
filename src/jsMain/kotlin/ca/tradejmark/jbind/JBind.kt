package ca.tradejmark.jbind

import ca.tradejmark.jbind.binds.AttributesBind
import ca.tradejmark.jbind.binds.TextBind
import ca.tradejmark.jbind.location.BindValueLocation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

object JBind {
    fun bind(root: HTMLElement, provider: Provider) {
        val binds = root.querySelectorAll("[${TextBind.attrName}],[${AttributesBind.attrName}]")
        for (i in 0 until binds.length) {
            val toBind = binds[i] as? HTMLElement ?: continue
            val textFlow = toBind.dataset[TextBind.datasetName]?.let {
                provider.getValue(BindValueLocation(it))
            }
            val attrFlows = toBind.dataset[AttributesBind.datasetName]
                ?.split(",")
                ?.map {
                    it to provider.getValue(BindValueLocation(toBind.getAttribute(it)!!))
                }
            JBindScope.launch { textFlow?.collect { toBind.innerText = it } }
            attrFlows?.forEach { (attr, valuesFlow) ->
                JBindScope.launch {
                    valuesFlow.collect { toBind.setAttribute(attr, it) }
                }
            }
        }
    }
}