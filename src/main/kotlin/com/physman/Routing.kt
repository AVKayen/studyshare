package com.physman

import com.physman.authentication.user.UserRepository
import com.physman.routes.*
import com.physman.forms.*
import com.physman.image.ImageRepository
import com.physman.task.TaskRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting(
    imageRepository: ImageRepository,
    solutionRepository: SolutionRepository,
    taskRepository: TaskRepository,
    userRepository: UserRepository
) {
    routing {
        staticFiles("/static", File("static"))

        get("/status") {
            call.respondText("Running!")
        }

        route("/auth") {
            authRouter(userRepository)
        }
        route("/forms") {
            configureForms(globalFormRouter)
        }
        route("/form-example") {
            formExampleRouter()
        }

        route("/solutions") {
            solutionRouter(solutionRepository)
        }

        route("/tasks") {
            taskRouter(taskRepository, solutionRepository)
        }

        route("/images") {
            imageRouter(imageRepository)
        }

        authenticate("USER") {
            route("/") {
                homeRouter()
            }
        }
    }
}

