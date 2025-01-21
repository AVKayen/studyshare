package com.physman

import com.physman.authentication.user.UserRepository
import com.physman.comment.CommentRepository
import com.physman.routes.*
import com.physman.forms.*
import com.physman.task.TaskRepository
import com.physman.solution.SolutionRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting(
    solutionRepository: SolutionRepository,
    taskRepository: TaskRepository,
    commentRepository: CommentRepository,
    userRepository: UserRepository
) {
    routing {
        staticFiles("/static", File("static"))

        get("/status") {
            call.respondText("Running!")
        }

        route("/forms") {
            configureForms(globalFormRouter)
        }

        route("/auth") {
            authRouter(userRepository)
        }

        route("/form-example") {
            formExampleRouter()
        }

        authenticate("USER") {

            route("/solutions") {
                solutionRouter(solutionRepository)
            }

            route("/tasks") {
                taskRouter(taskRepository)
            }

            route("/comments") {
                commentRouter(commentRepository, solutionRepository, taskRepository)
            }

            route("/") {
                homeRouter()
            }
        }
    }
}

