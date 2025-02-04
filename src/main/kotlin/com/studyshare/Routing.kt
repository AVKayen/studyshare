package com.studyshare

import com.studyshare.authentication.user.UserRepository
import com.studyshare.comment.CommentRepository
import com.studyshare.routes.*
import com.studyshare.forms.*
import com.studyshare.task.TaskRepository
import com.studyshare.solution.SolutionRepository
import com.studyshare.group.GroupRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
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
            indexViewRouter()

            authenticate("USER") {
                commentRouter(commentRepository, solutionRepository, taskRepository, groupRepository)
                groupRouter(groupRepository, userRepository)
                taskRouter(taskRepository, groupRepository)
                solutionRouter(solutionRepository, taskRepository, groupRepository)
            }
        }

        // legacy routers
        route("/forms") {
            configureForms(globalFormRouter)
        }
        route("/auth") {
            authRouter(userRepository)
        }
    }
}

