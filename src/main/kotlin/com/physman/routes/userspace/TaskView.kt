package com.physman.routes.userspace

import com.physman.authentication.user.UserSession
import com.physman.group.GroupRepository
import com.physman.task.TaskRepository
import com.physman.templates.formModalOpenButton
import com.physman.templates.index
import com.physman.templates.taskTemplate
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.article
import kotlinx.html.div

fun Route.taskViewRouter(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    route("/{groupId}") {
        route("/{taskId}") {
            getTaskView(taskRepository, groupRepository)
        }
    }
}

fun Route.getTaskView(taskRepository: TaskRepository, groupRepository: GroupRepository) {
    get {
        val objectIds = validateObjectIds(call, "taskId", "groupId") ?: return@get
        val taskId = objectIds["taskId"]!!
        val groupId = objectIds["groupId"]!!

        val taskView = taskRepository.getTask(taskId)
        if(taskView == null) {
            call.respondText(text = "Task not found.", status = HttpStatusCode.NotFound)
            return@get
        }

        val userSession = call.sessions.get<UserSession>()!!

        call.respondHtml(HttpStatusCode.OK) {
            index(
                title = "StudyShare",
                username = userSession.name,
                breadcrumbs = mapOf(groupId.toHexString() to "/${groupId}"),
                lastBreadcrumb = taskView.task.title
            ) {

                taskTemplate(taskView)
                formModalOpenButton(
                    buttonText = "Create a solution",
                    modalUrl = "/solutions/creation-modal?taskId=${taskView.task.id}"
                )
                div {
                    attributes["hx-get"] = "/solutions?taskId=${taskView.task.id}"
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