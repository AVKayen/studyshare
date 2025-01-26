package com.physman.routes

import com.physman.authentication.user.UserRepository
import com.physman.authentication.user.UserSession
import com.physman.group.GroupRepository
import com.physman.group.GroupView
import com.physman.templates.*
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

// Served at "/"
fun Route.homeRouter(groupRepository: GroupRepository, userRepository: UserRepository) {
    route("/") {
        get {
            val userSession = call.sessions.get<UserSession>()
            if (userSession != null) {
                call.respondHtml(HttpStatusCode.OK) {
                    index(
                        title = "StudyShare",
                        username = userSession.name
                    ) {
                        formModalOpenButton(
                            buttonText = "Create a group",
                            modalUrl = "/groups/creation-modal"
                        )
                        div {
                            attributes["hx-get"] = "/groups"
                            attributes["hx-trigger"] = "load"
                            attributes["hx-swap"] = "outerHTML"

                            article(classes = "htmx-indicator") {
                                attributes["aria-busy"] = "true"
                            }
                        }
                    }
                }
            } else {
                call.respondHtml(HttpStatusCode.OK) {
                    index(
                        title = "StudyShare",
                        username = null
                    ) {
                        p {
                            +"StudyShare is a platform for students to share tasks and solutions to them."
                        }
                        p {
                            +"Please log in or sign up to get started."
                        }
                        div {
                            id = "login-signup"
                            a(href = "/auth/login?redirectUrl=/") {
                                +"Log in"
                            }
                            +" | "
                            a(href = "/auth/register?redirectUrl=/") {
                                +"Sign up"
                            }
                        }
                    }
                }
            }
        }
    }
}