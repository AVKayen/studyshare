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

//     val dialogScript = """
//         def getScrollbarWidth()
//             return window.innerWidth - document.documentElement.clientWidth
//         end
         
//         on load 1
//             set scrollbarWidth to getScrollbarWidth()
//             if scrollbarWidth
//                 call document.documentElement.style.setProperty('$scrollbarWidthCssVar', scrollbarWidth + 'px')
//             end
//             add $openClass $openingClass to document.documentElement
//             add @open='true'
//             wait $animationDuration
//             remove $openingClass from document.documentElement
//         end
        
//         on closeModal
//             add $closingClass to document.documentElement
//             wait $animationDuration
//             remove $closingClass $openClass from document.documentElement
//             call document.documentElement.style.removeProperty('$scrollbarWidthCssVar')
//             remove me
//     """.trimIndent()

fun FlowContent.formModalDialog(form: Form, callbackUrl: String, requestType: String) {

    val formScript = """
        on htmx:afterRequest
            if event.srcElement is me and event.detail.successful
                trigger closeModal
            end
        end
    """.trimIndent()

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