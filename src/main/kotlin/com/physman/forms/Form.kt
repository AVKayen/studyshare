package com.physman.forms

import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.form
import kotlinx.html.h1

class Form(
    private val title: String,
    val callbackUrl: String,
    private val formAttributes: Map<String, String>? = null
) {
    var validatorsRoute: String? = null
    val routePath : String = title.lowercase().replace(" ", "_")
    var inputs : List<ControlledInput> = emptyList()
    private var isMultipart = false

    fun addInput(input: ControlledInput) {
        if (input is FileInput) {
            isMultipart = true
        }
        inputs = inputs.plus(input)
    }

    fun render(flowContent: FlowContent) {
        flowContent.form {
            attributes["hx-post"] = "/${callbackUrl}"

            if (isMultipart) {
                attributes["hx-encoding"] = "multipart/form-data"
            }

            if(this@Form.formAttributes != null) {
                attributes.putAll(formAttributes)
            }

            h1 { + this@Form.title }

            for (input in this@Form.inputs) {
                if (input is TextlikeInput) {
                    if (validatorsRoute != null) {
                        input.render(flowContent, url = this@Form.validatorsRoute!!)
                    } else {
                        // TODO: What error type to throw??
                        throw UninitializedPropertyAccessException("Form ${this@Form.title} is not routed")
                    }
                }
                if (input is FileInput) {
                    input.render(flowContent)
                }
            }
            button {
                +"Submit"
            }
        }
    }
}