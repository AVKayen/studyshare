package com.physman

import com.physman.authentication.user.UserRepository
import com.physman.comment.CommentRepository
import com.physman.routes.*
import com.physman.forms.*
import com.physman.task.TaskRepository
import com.physman.solution.SolutionRepository
import com.physman.group.GroupRepository
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
    userRepository: UserRepository,
    groupRepository: GroupRepository
) {
    routing {
        staticFiles("/static", File("static"))

        // neo routers
        route("/") {
            indexViewRouter(groupRepository, userRepository)

            authenticate("USER") {
                groupRouter(groupRepository, userRepository)
                taskRouter(taskRepository, groupRepository)
            }
        }

        // legacy routers
        route("/forms") {
            configureForms(globalFormRouter)
        }
        route("/auth") {
            authRouter(userRepository)
        }
        authenticate("USER") {
            route("/solutions") {
                solutionRouter(solutionRepository)
            }

            route("/comments") {
                commentRouter(commentRepository, solutionRepository, taskRepository)
            }
        }
    }
}

