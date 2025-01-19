package com.physman.forms

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.body

class FormRouter {
    val forms: MutableSet<Form> = mutableSetOf()

    fun routeFormValidators(form: Form) {
        forms.add(form)
        form.validatorsRoute = "/forms/${form.formName}"
    }
}

fun Route.configureForms(forms: FormRouter) {
    route("{form}") {

        post("{input}") {
            val form: Form? = forms.forms.find { it.formName == call.parameters["form"] }
            if (form == null) {
                call.response.status(HttpStatusCode.NotFound)
                call.respondText { "Form not found" }
            }

            val inputName = call.parameters["input"]!!

            val inputElement: ControlledInput? = form!!.inputs.find {
                it.inputName == inputName
            }
            if (inputName.isBlank() || inputElement !is TextlikeInput) {
                call.response.status(HttpStatusCode.BadRequest)
                return@post
            }

            val formParameters = call.receiveParameters()
            val inputtedString = formParameters[inputName].toString()

            call.respondHtml(HttpStatusCode.OK) {
                body {
                    inputElement.render(
                        flowContent = this,
                        inputtedString = inputtedString,
                        validationUrl = form.validatorsRoute!!,
                        replacePreviousInput = true
                    )
                }
            }
        }
    }
}

val globalFormRouter = FormRouter()