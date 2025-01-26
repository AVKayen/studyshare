package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.task.Task
import com.physman.task.TaskRepository
import com.physman.task.additionalNotesValidator
import com.physman.task.titleValidator
import com.physman.templates.*
import com.physman.utils.smartRedirect
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import com.physman.utils.validateObjectIds
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId

// TODO: Error handling
fun Route.taskRouter(taskRepository: TaskRepository) {

    val taskCreationForm = Form("Create a new task", "taskForm", formAttributes = mapOf(
//        "hx-target" to "#task-list",
//        "hx-swap" to "beforeend"
          "hx-swap" to "none" // because the form is on an empty page now
    ))

    taskCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    taskCreationForm.addInput(TextlikeInput("Additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    taskCreationForm.addInput(FileInput("Upload files", "files", inputAttributes = mapOf("multiple" to "true")))

    globalFormRouter.routeFormValidators(taskCreationForm)

    get {
        val taskViews = taskRepository.getTasks()
        call.respondHtml {
            body {
                for (task in taskViews) {
                    taskPreviewTemplate(task)
                }
            }
        }
    }

    get("/creation-modal") {
        call.respondHtml {
            body {
                formModalDialog(
                    form = taskCreationForm,
                    callbackUrl = "/tasks",
                    requestType = POST
                )
            }
        }
    }

    post {
        val formSubmissionData: FormSubmissionData = taskCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!
        val files = formSubmissionData.files

        val userSession = call.sessions.get<UserSession>()!!

        val task = Task(
            title = title,
            additionalNotes = additionalNotes,
            authorName = userSession.name,
            authorId = ObjectId(userSession.id),
            groupName = "FakeGroupName", // TODO: Replace with real group data
            groupId = ObjectId()
        )

        taskRepository.createTask(task, files)
        formSubmissionData.cleanup()

        call.smartRedirect(redirectUrl = "/tasks/${task.id}")
    }

    route("/{id}") {
        get {
            val objectIds = validateObjectIds(call, "id") ?: return@get
            val taskId = objectIds["id"]!!

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
                    breadcrumbs = mapOf("tasks" to "/"),
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

        delete {
            val objectIds = validateObjectIds(call, "id") ?: return@delete
            val taskId = objectIds["id"]!!

            taskRepository.deleteTask(taskId)

            call.response.status(HttpStatusCode.NoContent)
        }
    }
}