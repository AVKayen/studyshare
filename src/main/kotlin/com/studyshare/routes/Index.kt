package com.studyshare.routes

import com.studyshare.authentication.user.UserSession
import com.studyshare.templates.contentLoadTemplate
import com.studyshare.templates.modalOpenButton
import com.studyshare.templates.index
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
        h1 {
            +"Groups"
        }
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
            +"Please log in or sign up to get started:"
        }
        div {
            id = "login-signup"
            a(href = "/auth/login?redirectUrl=/") {
                +"Log in"
            }
            div(classes = "vertical-separator")
            a(href = "/auth/register?redirectUrl=/") {
                +"Sign up"
            }
        }
    }
}