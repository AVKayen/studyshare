package com.physman.forms

import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.body

class FormRouter {
    val forms: MutableSet<Form> = mutableSetOf()

    fun routeForm(form: Form) {
        forms.add(form)
        form.validatorsRoute = "/forms/${form.formName}"
    }
}

fun Route.configureForms(forms: FormRouter) {
    route("{form}") {

        get {
            // TODO: How to get rid of this duplicate code?
            val form: Form? = forms.forms.find { it.formName == call.parameters["form"] }
            if (form == null) {
                call.response.status(HttpStatusCode.NotFound)
                call.respondText { "Form not found" }
            }
            call.respondHtml {
                body {
                    form!!.render(this)
                }
            }
        }

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
                    inputElement.render(this, inputtedString, form.validatorsRoute!!)
                }
            }
        }
    }
}

val globalFormRouter = FormRouter()