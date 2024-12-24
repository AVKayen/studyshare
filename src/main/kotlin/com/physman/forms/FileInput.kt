package com.physman.forms

import kotlinx.html.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class FileInput(
    private val inputLabel: String,
    override val inputName: String,
    private val inputAttributes: Map<String, String>? = null
) : ControlledInput {

    init {
        if (inputName != URLEncoder.encode(inputName, StandardCharsets.UTF_8.toString())) {
            throw IllegalArgumentException("Invalid inputName. $inputName is not url-safe.")
        }
    }

    fun render(flowContent: FlowContent) {
        flowContent.div {
            label {
                attributes["for"] = inputName
                +inputLabel
            }
            input(type = InputType.file, name = inputName) {
                attributes["id"] = inputName
                if (inputAttributes != null) {
                    attributes.putAll(inputAttributes)
                }
            }
        }
    }
}