package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.comment.Comment
import com.physman.comment.CommentRepository
import com.physman.comment.commentValidator
import com.physman.forms.*
import com.physman.solution.SolutionRepository
import com.physman.task.TaskRepository
import com.physman.templates.commentCountTemplate
import com.physman.templates.commentTemplate
import com.physman.templates.index
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import kotlinx.html.*
import org.bson.types.ObjectId




fun Route.commentRouter(commentRepository: CommentRepository, solutionRepository: SolutionRepository, taskRepository: TaskRepository) {
    val commentCreationForm = Form(
        "Create a new comment", "commentForm", formAttributes = mapOf(
            "hx-swap" to "none" // because currently this form is on an empty page
        )
    )
    commentCreationForm.addInput(TextlikeInput("content", "content", InputType.text, commentValidator))

    globalFormRouter.routeFormValidators(commentCreationForm)

    //TODO: put in actual urls, or transfer values in an other way
    get {
        val objectIds: Map<String, ObjectId> = validateObjectIds(call, "parentId") ?: return@get
        val parentId = objectIds["parentId"]
        val parentPostClassName = call.request.queryParameters["postType"]!!
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        val comments = commentRepository.getComments(parentId!!)

        call.respondHtml {
            body {
                for (comment in comments) {
                    val isAuthor = userId == comment.authorId
                    commentTemplate(comment, isAuthor, parentPostClassName)
                }
                form {
                    attributes["hx-post"] = "/comments/comment?parentId=${parentId}&postType=${parentPostClassName}"
                    attributes["hx-target"] = "#comments-${parentId}"
                    textArea {
                        name = "content"
                        placeholder = "Write a comment..."
                    }
                    button {
                        type = ButtonType.submit
                        +"Comment"
                    }
                }

                span {
                    id = "comment-amount-${parentId}"
                    attributes["hx-swap-oob"] = "true"
                    commentCountTemplate(comments.size)
                }
            }
        }

    }

    route("/comment") {
        get {
            val parentId = call.request.queryParameters["parentId"]
            if (parentId == null) {
                call.respondText("No id specified.", status = HttpStatusCode.BadRequest)
                return@get
            }

            call.respondHtml {
                index("This won't be index") {
                    //TODO: maybe separate tasks from solutions
                    commentCreationForm.render(this, call.url(), POST)
                }
            }
        }

        post {
            val objectIds: Map<String, ObjectId> = validateObjectIds(call, "parentId") ?: return@post
            val parentId = objectIds["parentId"]
            val postType = call.request.queryParameters["postType"]
            val userSession = call.sessions.get<UserSession>()!!

            val formSubmissionData: FormSubmissionData = commentCreationForm.validateSubmission(call) ?: return@post
            val content = formSubmissionData.fields["content"]!!

            val newComment = Comment(parentId = parentId!!, content = content, authorName = userSession.name, authorId = ObjectId(userSession.id))

            if (postType.equals("task", true)) {
                taskRepository.updateCommentAmount(parentId, 1)
            } else if (postType.equals("solution", true)) {
                solutionRepository.updateCommentAmount(parentId, 1)
            } else {

                println(call.url())
                if (postType != null)
                    println(postType + postType.javaClass)
                return@post
            }

            commentRepository.createComment(newComment)

            call.respondRedirect("/comments?parentId=${parentId}&postType=${postType}")
        }
    }

    delete {
        println(1)
        val objectIds = validateObjectIds(call, "commentId", "parentId") ?: return@delete
        val commentId = objectIds["commentId"]!!
        val authorId = commentRepository.getComment(commentId)?.authorId

        val parentId = objectIds["parentId"]!!
        val postType = call.request.queryParameters["postType"]

        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)
        println(2)
        when (postType) {
            "task" -> {
                if (authorId == userId) {
                    taskRepository.updateCommentAmount(parentId, -1)
                } else {
                    call.respondText(
                        "Resource Modification Restricted - Ownership Required",
                        status = HttpStatusCode.Forbidden
                    )
                    return@delete
                }
            }
            "solution" -> {
                if (authorId == userId) {
                    solutionRepository.updateCommentAmount(parentId, -1)
                } else {
                    call.respondText(
                        "Resource Modification Restricted - Ownership Required",
                        status = HttpStatusCode.Forbidden
                    )
                    return@delete
                }
            }
            else -> {
                return@delete
            }
        }
        println(3)
        commentRepository.deleteComment(commentId)
        call.respondHtml { body() }
    }
}


























//when (postType) {
//    "task" -> {
//        if (isAuthor(commentId, parentId, userId, postType)) {
//            commentRepository.deleteComment(commentId)
//            taskRepository.updateCommentAmount(parentId, -1)
//            call.respondHtml { body() }
//        } else {
//            call.respondText(
//                "Resource Modification Restricted - Ownership Required",
//                status = HttpStatusCode.Forbidden
//            )
//            return@delete
//        }
//    }
//
//    "solution" -> {
//        if (isAuthor(commentId, parentId, userId, postType)) {
//            commentRepository.deleteComment(commentId)
//            solutionRepository.updateCommentAmount(parentId, -1)
//            call.respondHtml { body() }
//        } else {
//            call.respondText(
//                "Resource Modification Restricted - Ownership Required",
//                status = HttpStatusCode.Forbidden
//            )
//            return@delete
//        }
//    }
//
//    else -> {
//        return@delete
//    }
//}
