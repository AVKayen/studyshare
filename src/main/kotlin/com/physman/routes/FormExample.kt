package com.physman.routes

import com.physman.Form
import com.physman.TextlikeInput
import com.physman.UserSession
import com.physman.routeForm
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.InputType
import kotlinx.html.h1

fun Route.formRouter() {
    val nameValidator = {input : String? ->
        var errors : List<String> = emptyList()
        if (input.isNullOrBlank()) {
            errors = errors.plus("Input cannot be blank")
        }
        if (input == "Adam") {
            errors = errors.plus("Adam is not a name")
        }
        if (errors.isNotEmpty()) {
            errors
        } else {
            null
        }
    }
    val emailValidator = fun(input : String?) : List<String>? {
        var errors : List<String> = emptyList()
        if (input == null) {
            return null
        }
        if (input.isBlank()) {
            errors = errors.plus("Input cannot be blank")
        }
        if (!input.contains("@")) {
            errors = errors.plus("Email is not a name")
        }
        return errors.ifEmpty {
            null
        }
    }
    val form: Form = Form("This is a form", "sky")
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
        routeForm(form)
    }
}