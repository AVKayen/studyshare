package com.physman.templates

import com.physman.forms.Form
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.formModalOpenButton(buttonText: String, modalUrl: String) {
    button(classes = "btn primary") {
        attributes["hx-get"] = modalUrl
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        +buttonText
    }
}


fun FlowContent.formModalDialog(form: Form, callbackUrl: String, requestType: String) {

//    val animationDuration = "300ms"
//    val openClass = ".modal-is-open"
//    val openingClass = ".modal-is-opening"
//    val closingClass = ".modal-is-closing"
//    val scrollbarWidthCssVar = "--pico-scrollbar-width"
//
//    val dialogScript = """
//        def getScrollbarWidth()
//            return window.innerWidth - document.documentElement.clientWidth
//        end
//
//        on load 1
//            set scrollbarWidth to getScrollbarWidth()
//            if scrollbarWidth
//                call document.documentElement.style.setProperty('$scrollbarWidthCssVar', scrollbarWidth + 'px')
//            end
//            add $openClass $openingClass to body
//            add @open='true'
//            wait $animationDuration
//            remove $openingClass from body
//        end
//
//        on closeModal
//            add $closingClass to body
//            wait $animationDuration
//            remove $closingClass $openClass from body
//            call document.documentElement.style.removeProperty('$scrollbarWidthCssVar')
//            remove me
//    """.trimIndent()
//
    val formScript = """
        on htmx:afterRequest
            if event.srcElement is me
                if event.detail.successful
                    trigger closeModal
                end
            end
    """.trimIndent()
//
//    dialog {
//        attributes["_"] = dialogScript
//
//        div(classes = "modal-content") {
//            form.renderFormElement(flowContent = this, callbackUrl = callbackUrl, requestType = requestType, formHyperscript = formScript) {
//                article {
//                    header {
//                        button {
//                            attributes["type"] = "button"
//                            attributes["aria-label"] = "Close"
//                            attributes["rel"] = "prev"
//                            attributes["_"] = "on click trigger closeModal"
//                        }
//                        form.renderFormTitle(this)
//                    }
//
//                    form.renderInputFields(this)
//
//                    footer {
//                        button(classes = "secondary") {
//                            attributes["role"] = "button"
//                            attributes["type"] = "button"
//                            attributes["_"] = "on click trigger closeModal"
//
//                            +"Cancel"
//                        }
//
//                        form.renderFormSubmit(
//                            flowContent = this
//                        )
//                    }
//                }
//            }
//        }


    val modalWrapper: FlowContent.(block: FlowContent.() -> Unit) -> Unit = { block ->
        form.renderFormElement(flowContent = this, callbackUrl = callbackUrl, requestType = requestType, formHyperscript = formScript) {
            block()
        }
    }

    modalTemplate(
        title = form.formTitle,
        submitText = form.submitBtnText,
        submitAttributes = mapOf(),
        modalWrapper = modalWrapper
    ) {
        form.renderInputFields(this)
    }
}