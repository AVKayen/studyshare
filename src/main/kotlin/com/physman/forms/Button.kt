package com.physman.forms

import kotlinx.html.*


class Button(
    private val buttonText: String,
    private val buttonAttributes: Map<String, String>? = null
) {

    fun render(flowContent: FlowContent) {
        flowContent.button(type = ButtonType.button) {
            if (buttonAttributes != null) {
                attributes.putAll(buttonAttributes)
            }

            +buttonText
        }
    }
}

