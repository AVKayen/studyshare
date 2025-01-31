package com.physman.templates

import com.physman.forms.Form
import com.physman.forms.HtmxRequestType
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.formModalOpenButton(buttonText: String, modalUrl: String, additionalClasses: Set<String> = setOf()) {
    button {
        classes = setOf("btn", "primary") + additionalClasses
        attributes["hx-get"] = modalUrl
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        +buttonText
    }
}

fun FlowContent.formModalDialog(form: Form, callbackUrl: String, requestType: HtmxRequestType = HtmxRequestType.POST) {

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