package com.studyshare.templates

import kotlinx.html.*

fun FlowContent.confirmationModalTemplate(
    title: String, details: String, submitText: String, submitAttributes: Map<String, String>? = null
) {
    val predefinedSubmitAttributes = mapOf(
        "_" to "on click trigger closeModal"
    )
    modalTemplate(
        title = title,
        submitText = submitText,
        submitAttributes = if (submitAttributes != null) {
            predefinedSubmitAttributes + submitAttributes
        } else {
            predefinedSubmitAttributes
        }
    ) {
        span {
            +details
        }
    }
}
