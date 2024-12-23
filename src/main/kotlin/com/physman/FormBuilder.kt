package com.physman

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.html.*

interface ControlledInput {
    val routePath: String
}

class TextlikeInput(
    private val parameterName: String,
    private val type : InputType,
    private val validate : ((String) -> String?)?
) : ControlledInput {

    override val routePath = parameterName.lowercase().replace(" ", "_")

    fun render(flowContent: FlowContent, inputtedString: String? = null, url: String) {
        val error: String? = if(inputtedString != null) this.validate?.invoke(inputtedString) else null
        flowContent.div {

            attributes["hx-target"] = "this"
            attributes["hx-swap"] = "outerHTML"
            label {
                attributes["for"] = routePath
                +parameterName
            }
            input(type = type, name = routePath) {
                attributes["id"] = routePath

                if (error != null) {
                    attributes["aria-invalid"] = "true"
                }

                attributes["hx-post"] = "${url}/${routePath}"
                attributes["hx-trigger"] = "keyup changed delay:500ms"
                attributes["hx-sync"] = "closest form:abort"

                if (inputtedString != null) {
                    value = inputtedString
                }
            }
            if (error != null) {
                small {
                    +error
                }
            }
        }
    }
}

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