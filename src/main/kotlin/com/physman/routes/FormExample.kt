package com.physman.routes

import com.physman.forms.*
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.InputType

fun Route.formExampleRouter() {
    val nameValidator = {input : String? ->
        var errors = ""
        if (input.isNullOrBlank()) {
            errors += " Input cannot be blank"
        }
        if (input == "Adam") {
            errors += " Adam is not a name"
        }
        if (errors.isNotEmpty()) {
            errors.trim()
        } else {
            null
        }
    }
    val emailValidator = fun(input : String?) : String? {
        var errors = ""
        if (input == null) {
            return null
        }
        if (input.isBlank()) {
            errors += " Input cannot be blank"
        }
        if (!input.contains("@")) {
            errors += "Email is not a name"
        }
        return errors.ifEmpty {
            null
        }
    }
    val form = Form("This is an example form", "exampleForm")
    form.addInput(TextlikeInput("Name", "name", InputType.text, nameValidator))
    form.addInput(TextlikeInput("Email", "email", InputType.text, emailValidator))

    globalFormRouter.routeFormValidators(form)

    get {
        call.respondHtml(HttpStatusCode.OK) {
            index(title = "Example form page") {
                form.render(this, "form-example")
            }
        }
    }
    post {
        call.respondHtml(HttpStatusCode.OK) {}
    }
}