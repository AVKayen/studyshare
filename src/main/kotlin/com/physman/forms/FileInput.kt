package com.physman.forms

import kotlinx.html.*

class FileInput(
    private val parameterName: String,
    private val inputAttributes: Map<String, String>? = null
) : ControlledInput {

    override val routePath = parameterName.lowercase().replace(" ", "_")

    fun render(flowContent: FlowContent) {
        flowContent.div {
            label {
                attributes["for"] = routePath
                +parameterName
            }
            input(type = InputType.file, name = routePath) {
                attributes["hx-preserve"] = "true"
                if (inputAttributes != null) {
                    attributes.putAll(inputAttributes)
                }
            }
        }
    }
}