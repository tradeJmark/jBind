package ca.tradejmark.jbind

import org.w3c.dom.HTMLElement

internal object JsUtil {
    val HTMLElement.nextHTMLElementSibling get() = nextElementSibling as? HTMLElement
}