package com.studyshare.templates

import kotlinx.html.*

private fun FlowContent.modalContent(title: String, secondaryButtonText: String?, submitText: String?, submitAttributes: Map<String, String>?, modalBody: ARTICLE.() -> Unit) {
    article {
        header {
            h2 {
                +title
            }
            button {
                attributes["type"] = "button"
                attributes["aria-label"] = "Close"
                attributes["rel"] = "prev"
                attributes["_"] = "on click trigger closeModal"
            }
        }

        modalBody()

        if (secondaryButtonText != null || submitText != null) {
            footer {
                if (secondaryButtonText != null) {
                    button(classes = "secondary") {
                        attributes["role"] = "button"
                        attributes["type"] = "button"
                        attributes["_"] = "on click trigger closeModal"

                        +secondaryButtonText
                    }
                }
                if (submitText != null) {
                    button {
                        if (submitAttributes != null) {
                            attributes.putAll(submitAttributes)
                        }

                        span(classes = "htmx-indicator") {
                            attributes["aria-busy"] = "true"
                        }
                        +submitText
                    }
                }
            }
        }
    }
}


fun FlowContent.modalTemplate(
    title: String,
    submitText: String? = null,
    submitAttributes: Map<String, String>? = null,
    secondaryButtonText: String? = "Cancel",
    modalWrapper: (FlowContent.(block: FlowContent.() -> Unit) -> Unit)? = null,
    modalBody: ARTICLE.() -> Unit
) {

    val animationDuration = "300ms"
    val openClass = ".modal-is-open"
    val openingClass = ".modal-is-opening"
    val closingClass = ".modal-is-closing"
    val scrollbarWidthCssVar = "--pico-scrollbar-width"

    val dialogScript = """
        def getScrollbarWidth()
            return window.innerWidth - document.documentElement.clientWidth
        end
         
        on load 1
            set scrollbarWidth to getScrollbarWidth()
            if scrollbarWidth
                call document.documentElement.style.setProperty('$scrollbarWidthCssVar', scrollbarWidth + 'px')
            end
            add $openClass $openingClass to document.documentElement
            add @open='true'
            wait $animationDuration
            remove $openingClass from document.documentElement
        end
        
        on closeModal
            add $closingClass to document.documentElement
            wait $animationDuration
            remove $closingClass $openClass from document.documentElement
            call document.documentElement.style.removeProperty('$scrollbarWidthCssVar')
            remove me
        end
    """.trimIndent()

    dialog(classes = "modal") {
        attributes["_"] = dialogScript

        div(classes = "modal-content") {
            if (modalWrapper != null) {
                modalWrapper {
                    modalContent(title, secondaryButtonText, submitText, submitAttributes, modalBody)
                }
            } else {
                modalContent(title, secondaryButtonText, submitText, submitAttributes, modalBody)
            }
        }
    }

}
