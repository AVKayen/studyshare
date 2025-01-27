package com.physman.routes

import com.physman.authentication.user.UserRepository
import com.physman.authentication.user.UserSession
import com.physman.group.GroupRepository
import com.physman.templates.contentLoadTemplate
import com.physman.templates.formModalOpenButton
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Route.indexViewRouter(groupRepository: GroupRepository, userRepository: UserRepository) {
    route("/") {
        getIndexView(groupRepository, userRepository)
    }
}

fun Route.getIndexView(groupRepository: GroupRepository, userRepository: UserRepository) {
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
            formModalOpenButton(
                buttonText = "Create a group",
                modalUrl = "/groups/creation-modal",
                additionalClasses = setOf("group-create-button", "wide-button", "outline")
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