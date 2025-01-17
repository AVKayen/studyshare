package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.task.Task
import com.physman.task.TaskRepository
import com.physman.task.additionalNotesValidator
import com.physman.task.titleValidator
import com.physman.templates.index
import com.physman.templates.taskPreviewTemplate
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import com.physman.templates.taskTemplate
import com.physman.utils.validateObjectIds
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.html.*

// TODO: Error handling
fun Route.taskRouter(taskRepository: TaskRepository) {

    val taskCreationForm = Form("Create a new task", "taskForm", mapOf(
//        "hx-target" to "#task-list",
//        "hx-swap" to "beforeend"
          "hx-swap" to "none" // because the form is on an empty page now
    ))

    taskCreationForm.addInput(TextlikeInput("title", "title", InputType.text, titleValidator))
    taskCreationForm.addInput(TextlikeInput("additional notes", "additionalNotes", InputType.text, additionalNotesValidator))
    taskCreationForm.addInput(FileInput("files", "files", acceptedTypes = listOf("image/*")))

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

    get("/creation-form") {
        call.respondHtml {
//            body {
//                taskCreationForm.render(this, "/tasks")
//            }

            // index because of lack of htmx needed for testing (htmx is served with index page only)
            index("This won't be index") {
                taskCreationForm.render(this, "/tasks")
            }
        }
    }

    post {
        val formSubmissionData: FormSubmissionData = taskCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val additionalNotes = formSubmissionData.fields["additionalNotes"]!!
        val files = formSubmissionData.files
        print(files)

        val newTask = Task(title = title, additionalNotes = additionalNotes)

        taskRepository.createTask(newTask, files)
        formSubmissionData.cleanup()

        call.response.status(HttpStatusCode.NoContent)
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

                    div {
                        attributes["hx-get"] = "/solutions?taskId=${taskView.task.id}"
                        attributes["hx-trigger"] = "load"

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