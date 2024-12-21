package com.physman

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

class TextlikeInput(val parameterName: String, val type : InputType, val validate : (String) -> String?) {
    val routePath = parameterName.lowercase().replace(" ", "_")

    fun render(flowContent: FlowContent, inputtedString: String?, url: String) {
        val error: String? = if(inputtedString != null) this.validate(inputtedString) else null
        flowContent.div {
            attributes["hx-target"] = "this"
            attributes["hx-swap"] = "outerHTML"
            label {
                attributes["for"] = routePath
                +parameterName
            }
            input(type = type, name = routePath) {
                if (error != null) {
                    attributes["aria-invalid"] = "true"
                }
                attributes["hx-post"] = "${url}/${routePath}"
                attributes["hx-trigger"] = "keyup changed delay:300ms"
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

class Form(val title: String, val callbackUrl: String, val formAttributes: Map<String, String>? = null) {
    val routePath : String = title.lowercase().replace(" ", "_")
    var inputs : List<TextlikeInput> = emptyList()
    fun addInput(input: TextlikeInput) {
        inputs = inputs.plus(input)
    }
    fun render(flowContent: FlowContent) {
        flowContent.form {
            attributes["hx-post"] = "/${callbackUrl}"

            if(this@Form.formAttributes != null) {
                attributes.putAll(formAttributes)
            }

            h1 { + this@Form.title }
            for (input in this@Form.inputs) {
                input.render(flowContent, inputtedString = null, url = "${callbackUrl}/${routePath}")
            }
            button {
                +"Submit"
            }
        }
    }
}

fun Route.routeForm(form: Form) {
    route(form.routePath) {
        get {
            call.respondHtml {
                body {
                    form.render(this)
                }
            }
        }
        post("{input}") {
            val inputName = call.parameters["input"]!!
            println(inputName)
            val inputElement: TextlikeInput? = form.inputs.find {
                it.routePath == inputName
            }
            if (inputElement == null || inputName.isBlank()) {
                call.respondText("What")
                return@post
            }
            val formParameters = call.receiveParameters()
            val inputtedString = formParameters[inputName].toString()
            call.respondHtml(HttpStatusCode.OK) {
                body {
                    inputElement.render(this, inputtedString, "${form.callbackUrl}/${form.routePath}")
                }
            }
        }
    }
}