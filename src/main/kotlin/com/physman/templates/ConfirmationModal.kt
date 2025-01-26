package com.physman.templates

import kotlinx.html.*

fun FlowContent.confirmationModalTemplate(title: String, details: String, submitText: String, submitAttributes: Map<String, String>) {

    modalTemplate(
        title = title,
        submitText = submitText,
        submitAttributes = mapOf("_" to "on click trigger closeModal") + submitAttributes
    ) {
        span {
            +details
        }
    }
}
