package com.physman.routes

import com.physman.forms.*
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.InputType

fun Route.formRouter() {
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
    val form = Form("This is a form", "sky")
    form.addInput(TextlikeInput("Name", InputType.text, nameValidator))
    form.addInput(TextlikeInput("Email", InputType.text, emailValidator))

    route("/sky") {
        get {
            call.respondHtml(HttpStatusCode.OK) {
                index(title = "String") {
                    form.render(this)
                }
            }
        }
        post {
            call.respondHtml(HttpStatusCode.OK) {}
        }
        globalFormRouter.routeForm(form)
    }
}