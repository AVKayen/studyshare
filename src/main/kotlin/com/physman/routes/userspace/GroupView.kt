package com.physman.routes.userspace

import com.physman.authentication.user.UserRepository
import com.physman.authentication.user.UserSession
import com.physman.group.GroupRepository
import com.physman.templates.formModalOpenButton
import com.physman.templates.index
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.FlowContent
import kotlinx.html.article
import kotlinx.html.div
import kotlinx.html.section

fun Route.groupViewRouter(groupRepository: GroupRepository, userRepository: UserRepository) {
    route("/{groupId}") {
        getGroupView(groupRepository, userRepository)
    }
}

fun Route.getGroupView(groupRepository: GroupRepository, userRepository: UserRepository) {
    get {
        val objectIds = validateObjectIds(call, "groupId") ?: return@get
        val groupId = objectIds["groupId"]!!

        val groupView = groupRepository.getGroup(groupId) ?: return@get call.respond(HttpStatusCode.NotFound)
        val userSession = call.sessions.get<UserSession>()!!
        call.respondHtml(HttpStatusCode.OK) {
            index(
                title = "StudyShare",
                username = userSession.name,
                lastBreadcrumb = groupView.group.title
            ) {
                section(classes = "modal-btn-container") {
                    formModalOpenButton(
                        buttonText = "Create a task",
                        modalUrl = "/tasks/creation-modal"
                    )
                }
                div {
                    attributes["hx-get"] = "/${groupId}/tasks"
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