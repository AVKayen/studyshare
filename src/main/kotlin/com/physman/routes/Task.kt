package com.physman.routes

import com.physman.forms.*
import com.physman.solution.Solution
import com.physman.task.InMemoryTaskRepository
import com.physman.task.Task
import com.physman.templates.index
import com.physman.templates.solutionTemplate
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import com.physman.templates.taskTemplate
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.html.InputType
import kotlinx.html.article
import kotlinx.html.body
import kotlinx.html.div
import kotlin.random.Random

const val TITLE_MAX_LENGTH = 20
const val ADDITIONAL_NOTES_MAX_LENGTH = 50

fun Route.taskRouter() {

    val titleValidator = fun(title: String): String? {
         if(title.isEmpty()) {
            return "Title must not be empty"
         }
         if (title.length > TITLE_MAX_LENGTH) {
            return "Title too long (max length $TITLE_MAX_LENGTH)"
         }
         return null
    }

    val additionalNotesValidator = fun(additionalNotes: String): String? {
        if(additionalNotes.length > ADDITIONAL_NOTES_MAX_LENGTH) {
            return "Additional notes too long (max length $ADDITIONAL_NOTES_MAX_LENGTH)"
        }
        return null
    }

    val taskCreationForm = Form("Create a new task", "tasks", mapOf(
        "hx-target" to "#task-list",
        "hx-swap" to "beforeend"
    ))
    taskCreationForm.addInput(TextlikeInput("title", InputType.text, titleValidator))
    taskCreationForm.addInput(TextlikeInput("additional notes", InputType.text, additionalNotesValidator))


    fun solutionCreationFormCreator(taskId: Int): Form{
        val solutionCreationForm = Form("Create a new solution", "tasks/${taskId}/solutions", mapOf(
            "hx-target" to "#solution-list",
            "hx-swap" to "beforeend"
        ))
        solutionCreationForm.addInput(TextlikeInput("title", InputType.text, titleValidator))
        solutionCreationForm.addInput(TextlikeInput("additional notes", InputType.text, additionalNotesValidator))
        return solutionCreationForm
    }

    globalFormRouter.routeForm(taskCreationForm)
    // i have no clue why this works (but it works)
    globalFormRouter.routeForm(solutionCreationFormCreator(0))

    get {
        val tasks = InMemoryTaskRepository.getAllTasks()
        call.respondHtml {
            index("Tasks") {
                div {
                    attributes["id"] = "task-list"
                    for (task in tasks) {
                        taskTemplate(task)
                    }
                }
                article {
                    taskCreationForm.render(this)
                }
            }
        }
    }

    post {
        val formParameters = call.receiveParameters()
        val title = formParameters["title"].toString()
        val additionalNotes = formParameters["additional_notes"].toString()

        val error: String? = titleValidator(title) ?: additionalNotesValidator(additionalNotes)
        if(error != null) {
            call.respondText(error, status = HttpStatusCode.BadRequest)
        }

        val newTask = Task(id = Random.nextInt(99999), title = title, additionalNotes = additionalNotes)

        val task = InMemoryTaskRepository.createTask(newTask)

        call.respondHtml(HttpStatusCode.OK) {
            body {
                taskTemplate(task)
            }
        }
    }

    route("/{id}") {
        get {
            val taskId = call.parameters["id"]?.toIntOrNull()
            if(taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@get
            }

            val task = InMemoryTaskRepository.getTask(taskId)
            if(task == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@get
            }

            call.respondHtml(HttpStatusCode.OK) {
                index("Task") {
                    taskTemplate(task)
                }
            }
        }

        delete {
            val taskId = call.parameters["id"]?.toIntOrNull()
            if(taskId == null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@delete
            }

            val deletedTask = InMemoryTaskRepository.deleteTask(taskId)
            if(deletedTask == null) {
                call.response.status(HttpStatusCode.NotFound)
                return@delete
            }
            call.response.status(HttpStatusCode.NoContent)
        }


        route("/solutions") {
            get {
                val taskId = call.parameters["id"]?.toIntOrNull()
                if(taskId == null) {
                    call.response.status(HttpStatusCode.BadRequest)
                    return@get
                }

                val solutions = InMemoryTaskRepository.getAllSolutions(taskId)
                if(solutions == null) {
                    call.response.status(HttpStatusCode.NotFound)
                    return@get
                }
                call.respondHtml {
                    index("Solutions") {
                        div {
                            attributes["id"] = "solution-list"
                            for (solution in solutions) {
                                solutionTemplate(solution)
                            }
                        }
                        article {
                            solutionCreationFormCreator(taskId).render(this)
                        }
                    }
                }
            }
            post {
                val taskId = call.parameters["id"]?.toIntOrNull()
                if (taskId == null) {
                    call.response.status(HttpStatusCode.BadRequest)
                    return@post
                }
                val formParameters = call.receiveParameters()
                val title = formParameters["title"].toString()
                val additionalNotes = formParameters["additional_notes"].toString()

                val error: String? = titleValidator(title) ?: additionalNotesValidator(additionalNotes)
                if (error != null) {
                    call.respondText(error, status = HttpStatusCode.BadRequest)
                }

                val newSolution = Solution(id = Random.nextInt(99999), title = title, additionalNotes = additionalNotes)

                val solution = InMemoryTaskRepository.createSolution(taskId, newSolution)
                if (solution == null) {
                    call.response.status(HttpStatusCode.NotFound)
                    return@post
                }

                call.respondHtml(HttpStatusCode.OK) {
                    body {
                        solutionTemplate(solution)
                    }
                }
            }
            route("/{solutionId}") {
                get {
                    val taskId = call.parameters["id"]?.toIntOrNull()
                    if(taskId == null) {
                        call.response.status(HttpStatusCode.BadRequest)
                        return@get
                    }
                    val solutionId = call.parameters["solutionId"]?.toIntOrNull()
                    if(solutionId == null) {
                        call.response.status(HttpStatusCode.BadRequest)
                        return@get
                    }

                    val solution = InMemoryTaskRepository.getSolution(taskId, solutionId)
                    if(solution == null) {
                        call.response.status(HttpStatusCode.NotFound)
                        return@get
                    }


                    call.respondHtml(HttpStatusCode.OK) {
                        index("Solution") {
                            solutionTemplate(solution)
                        }
                    }
                }
                delete {
                    val taskId = call.parameters["id"]?.toIntOrNull()
                    if(taskId == null) {
                        call.response.status(HttpStatusCode.BadRequest)
                        return@delete
                    }
                    val solutionId = call.parameters["solutionId"]?.toIntOrNull()
                    if(solutionId == null) {
                        call.response.status(HttpStatusCode.BadRequest)
                        return@delete
                    }

                    val deletedSolution = InMemoryTaskRepository.deleteSolution(taskId, solutionId)
                    if(deletedSolution == null) {
                        call.response.status(HttpStatusCode.NotFound)
                        return@delete
                    }
                    call.response.status(HttpStatusCode.NoContent)
                }
            }
        }
    }
}