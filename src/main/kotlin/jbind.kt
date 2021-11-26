package ca.tradejmark.jbind

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.dom.isElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

const val itemDatasetName = "jbindItem"
const val itemAttrName = "data-jbind-item"

fun bind(root: HTMLElement, provider: Provider) {
    val itemBinds = root.querySelectorAll("[$itemAttrName]")
    for (i in 0 until itemBinds.length) {
        if (!itemBinds[i]!!.isElement) continue
        val toBind = itemBinds[i] as HTMLElement
        val location = toBind.dataset[itemDatasetName]!!
        val values = provider.getString(BindLoc.parse(location))
        JBindScope.launch {
            values.collect {
                toBind.innerText = it
            }
        }
    }
}