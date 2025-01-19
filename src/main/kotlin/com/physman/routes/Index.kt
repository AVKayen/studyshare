package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.templates.*
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

// Served at "/"
fun Route.homeRouter() {
    route("/") {
        get {
            val userSession = call.sessions.get<UserSession>()!!
            call.respondHtml(HttpStatusCode.OK) {
                index(
                    title = "StudyShare",
                    username = userSession.name
                ) {
                    section(classes = "modal-btn-container") {
                        formModalOpenButton(
                            buttonText = "Create a task",
                            modalUrl = "/tasks/creation-modal"
                        )
                    }
                    div {
                        attributes["hx-get"] = "/tasks"
                        attributes["hx-trigger"] = "load"
                        attributes["hx-swap"] = "outerHTML"

                        article(classes = "htmx-indicator") {
                            attributes["aria-busy"] = "true"
                        }
                    }
                }
            }
        }
    }
}