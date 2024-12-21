package com.physman

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

class TextlikeInput(val parameterName: String, val type : InputType, val validate : (String?) -> List<String>?) {
    val routePath = parameterName.lowercase().replace(" ", "_")

    fun render(flowContent: FlowContent, inputtedString: String?, url: String) {
        val errors: List<String>? = this.validate(inputtedString)
        flowContent.div {
            attributes["hx-target"] = "this"
            attributes["hx-swap"] = "outerHTML"
            label {
                attributes["for"] = routePath
                +parameterName
            }
            input(type = type, name = routePath) {
                if (errors != null) {
                    attributes["aria-invalid"] = "true"
                }
                attributes["hx-post"] = "${url}/${routePath}"
                if (inputtedString != null) {
                    value = inputtedString
                }
            }
            if (errors != null) {
                small {
                    +errors.joinToString(", ")
                }
            }
        }
    }
}

class Form(val title: String, val callbackUrl: String) {
    val routePath : String = "/api/forms/${title.lowercase().replace(" ", "_")}"
    var inputs : List<TextlikeInput> = emptyList()
    fun addInput(input: TextlikeInput) {
        inputs = inputs.plus(input)
    }
    fun render(flowContent: FlowContent) {
        flowContent.form {
            attributes["hx-post"] = "/${callbackUrl}"

            h1 { + this@Form.title }
            for (input in this@Form.inputs) {
                input.render(flowContent, inputtedString = null, url = routePath)
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
            val inputElement: TextlikeInput? = form.inputs.find {
                it.parameterName == inputName
            }
            if (inputElement == null || inputName.isBlank()) {
                call.respondText("What")
                return@post
            }
            val formParameters = call.receiveParameters()
            val inputtedString = formParameters[inputName].toString()
            call.respondHtml(HttpStatusCode.OK) {
                body {
                    inputElement.render(this, inputtedString, form.routePath)
                }
            }
        }
    }
}