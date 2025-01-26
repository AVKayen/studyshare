package com.physman.templates

import kotlinx.html.*

fun FlowContent.confirmationModalTemplate(title: String, details: String, submitText: String, submitAttributes: Map<String, String>) {

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
            log "opening"
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
            log "closing modal"
            add $closingClass to body
            wait $animationDuration
            remove $closingClass $openClass from body
            call document.documentElement.style.removeProperty('$scrollbarWidthCssVar')
            remove me
    """.trimIndent()

    dialog {
        attributes["_"] = dialogScript

        div(classes = "modal-content") {
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

                +details

                footer {
                    button(classes = "secondary") {
                        attributes["role"] = "button"
                        attributes["type"] = "button"
                        attributes["_"] = "on click trigger closeModal"

                        +"Cancel"
                    }

                    button {
                        attributes["_"] = "on click trigger closeModal"
                        attributes.putAll(submitAttributes)
                        +submitText
                    }
                }
            }
        }
    }
}
