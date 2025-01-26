package com.physman.templates

import kotlinx.html.*

private fun FlowContent.modalContent(title: String, submitText: String, submitAttributes: Map<String, String>, modalBody: ARTICLE.() -> Unit) {
    article {
        header {
            button {
                attributes["type"] = "button"
                attributes["aria-label"] = "Close"
                attributes["rel"] = "prev"
                attributes["_"] = "on click trigger closeModal"
            }
            h2 {
                +title
            }
        }

        modalBody()

        footer {
            button(classes = "secondary") {
                attributes["role"] = "button"
                attributes["type"] = "button"
                attributes["_"] = "on click trigger closeModal"

                +"Cancel"
            }

            button {
                attributes.putAll(submitAttributes)
                +submitText
            }
        }
    }
}


fun FlowContent.modalTemplate(
    title: String,
    submitText: String,
    submitAttributes: Map<String, String>,
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
            add $openClass $openingClass to body
            add @open='true'
            wait $animationDuration
            remove $openingClass from body
        end
        
        on closeModal
            add $closingClass to body
            wait $animationDuration
            remove $closingClass $openClass from body
            call document.documentElement.style.removeProperty('$scrollbarWidthCssVar')
            remove me
    """.trimIndent()

    dialog {
        attributes["_"] = dialogScript
        // lol
        div(classes = "modal-content") {
            if (modalWrapper != null) {
                modalWrapper {
                    modalContent(title, submitText, submitAttributes, modalBody)
                }
            } else {
                modalContent(title, submitText, submitAttributes, modalBody)
            }
        }
    }

}
