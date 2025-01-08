package com.physman.routes

import com.physman.forms.*
import com.physman.solution.SolutionRepository
import com.physman.task.Task
import com.physman.task.TaskRepository
import com.physman.templates.index
import com.physman.templates.solutionTemplate
import com.physman.templates.taskPreviewTemplate
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import com.physman.templates.taskTemplate
import com.physman.utils.additionalNotesValidator
import com.physman.utils.titleValidator
import com.physman.utils.validateObjectIds
import io.ktor.server.response.*
import kotlinx.html.InputType
import kotlinx.html.body



fun Route.taskRouter(taskRepository: TaskRepository, solutionRepository: SolutionRepository) {

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
        val files = formSubmissionData.files!!

        formSubmissionData.cleanup()

        val newTask = Task(title = title, additionalNotes = additionalNotes)

        val success = taskRepository.createTask(newTask)
        if (!success) {
            call.respondText(text = "Task has not been created.", status = HttpStatusCode.BadRequest)
            return@post
        }

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

            val solutions = solutionRepository.getSolutions(taskId)
            // TODO Sorry, I've broken the sorting

            call.respondHtml(HttpStatusCode.OK) {
                index("Task") {

                    taskTemplate(taskView)

                    for (solution in solutions){
                        solutionTemplate(solution, taskId.toString())
                    }
                }
            }
        }

        delete {
            val objectIds = validateObjectIds(call, "id") ?: return@delete
            val taskId = objectIds["id"]!!

            val success = taskRepository.deleteTask(taskId)
            if(!success) {
                call.respondText(text = "Task has not been deleted.", status = HttpStatusCode.BadRequest)
                return@delete
            }
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}