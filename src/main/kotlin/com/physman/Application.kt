package com.physman

import com.physman.attachment.MongoGCloudAttachmentRepository
import com.physman.authentication.user.MongoUserRepository
import com.physman.authentication.configureSecurity
import io.ktor.server.application.*

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.physman.comment.MongoCommentRepository
import com.physman.solution.MongoSolutionRepository
import com.physman.task.MongoTaskRepository
import com.physman.templates.index
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.StatusPages
import kotlinx.html.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val environment = Environment(true)
    val mongodbClient = MongoClient.create(environment.MONGODB_CONNECTION_STRING)
    //TODO: change this
    val database = mongodbClient.getDatabase("buttonstudyshare")

    val imageRepository = MongoGCloudAttachmentRepository(bucketName = "studyshare-files", database = database)
    val commentRepository = MongoCommentRepository(database)
    val solutionRepository = MongoSolutionRepository(database, commentRepository, imageRepository)
    val taskRepository = MongoTaskRepository(database, imageRepository, commentRepository, solutionRepository)
    val userRepository = MongoUserRepository(database)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondHtml(HttpStatusCode.InternalServerError) {
                index("Error") {
                    h1 {
                        +"A server error occurred: ${cause.localizedMessage}."
                    }
                    p {
                        +"It's not your fault, it's ours. Please try again later."
                    }
                }
            }
        }
        status(HttpStatusCode.NotFound) { call, _ -> // cause is redundant to know (it's always page not found)
            call.respondHtml {
                index("Not Found") {
                    h1 {
                        +"Page not found"
                    }
                }
            }
        }
    }

    configureSecurity()
    configureRouting(
        solutionRepository = solutionRepository,
        taskRepository = taskRepository,
        userRepository = userRepository,
        commentRepository = commentRepository,
    )
}
