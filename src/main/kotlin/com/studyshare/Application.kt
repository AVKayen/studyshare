package com.studyshare

import com.studyshare.attachment.MongoGCloudAttachmentRepository
import com.studyshare.authentication.user.MongoUserRepository
import com.studyshare.authentication.configureSecurity
import io.ktor.server.application.*

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.studyshare.comment.MongoCommentRepository
import com.studyshare.environment.Environment
import com.studyshare.environment.GoogleDatastoreEnvironment
import com.studyshare.group.MongoGroupRepository
import com.studyshare.solution.MongoSolutionRepository
import com.studyshare.task.MongoTaskRepository
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    EngineMain.main(args)
}

var globalEnvironment: Environment? = null

private val logger = LoggerFactory.getLogger("Application")


fun Application.module() {
    // You may change this to use your own Environment class implementation
    globalEnvironment = GoogleDatastoreEnvironment(!this.developmentMode)

    val mongodbClient = MongoClient.create(globalEnvironment!!.mongodbConnectionString)
    val database = mongodbClient.getDatabase(globalEnvironment!!.databaseName)

    val imageRepository = MongoGCloudAttachmentRepository(bucketName = globalEnvironment!!.bucketName, database = database)
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
