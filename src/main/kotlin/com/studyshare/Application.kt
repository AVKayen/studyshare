package com.studyshare

import com.studyshare.attachment.MongoGCloudAttachmentRepository
import com.studyshare.authentication.user.MongoUserRepository
import com.studyshare.authentication.configureSecurity
import io.ktor.server.application.*

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.studyshare.comment.MongoCommentRepository
import com.studyshare.group.MongoGroupRepository
import com.studyshare.solution.MongoSolutionRepository
import com.studyshare.task.MongoTaskRepository
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    EngineMain.main(args)
}

var isDevelopment: Boolean = false
private val logger = LoggerFactory.getLogger("Application")


fun Application.module() {
    // check if in production or development mode
    isDevelopment = this.developmentMode
    val environment = Environment(true)
    val mongodbClient = MongoClient.create(environment.MONGODB_CONNECTION_STRING)
    val database = mongodbClient.getDatabase("studyshare")

    val imageRepository = MongoGCloudAttachmentRepository(bucketName = "studyshare-files", database = database)
    val commentRepository = MongoCommentRepository(database)
    val solutionRepository = MongoSolutionRepository(database, commentRepository, imageRepository)
    val taskRepository = MongoTaskRepository(database, imageRepository, commentRepository, solutionRepository)
    val userRepository = MongoUserRepository(database)
    val groupRepository = MongoGroupRepository(database, taskRepository, imageRepository, userRepository)

    configureStatusPages(logger)
    configureSecurity(userRepository)
    configureRouting(
        solutionRepository = solutionRepository,
        taskRepository = taskRepository,
        userRepository = userRepository,
        commentRepository = commentRepository,
        groupRepository = groupRepository
    )
}
