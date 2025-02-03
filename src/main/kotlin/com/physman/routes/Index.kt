package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.templates.contentLoadTemplate
import com.physman.templates.modalOpenButton
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.indexViewRouter() {
    route("/") {
        getIndexView()
    }
}

fun Route.getIndexView() {
    get {
        val userSession = call.sessions.get<UserSession>()
        if (userSession != null) {
            call.respondHtml(HttpStatusCode.OK) {
                loggedInIndexView(userSession)
            }
        } else {
            call.respondHtml(HttpStatusCode.OK) {
                entryIndexView()
            }
        }
    }
}

fun HTML.loggedInIndexView(userSession: UserSession) {
    index(
        title = "StudyShare",
        username = userSession.name
    ) {
        div {
            classes = setOf("wide-button-container")
            modalOpenButton(
                buttonText = "Create a group",
                modalUrl = "/groups/creation-modal"
            )
        }
        contentLoadTemplate("/groups")
    }
}

fun HTML.entryIndexView() {
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