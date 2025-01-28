package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.comment.Comment
import com.physman.comment.CommentRepository
import com.physman.comment.commentValidator
import com.physman.solution.SolutionRepository
import com.physman.task.TaskRepository
import com.physman.templates.commentCountTemplate
import com.physman.templates.commentTemplate
import com.physman.templates.index
import com.physman.utils.validateRequiredObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId




fun Route.commentRouter(commentRepository: CommentRepository, solutionRepository: SolutionRepository, taskRepository: TaskRepository) {

    //TODO: put in actual urls, or transfer values in an other way
    get {
        val objectIds: Map<String, ObjectId> = validateRequiredObjectIds(call, "parentId") ?: return@get

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
        post {
            val objectIds: Map<String, ObjectId> = validateRequiredObjectIds(call, "parentId") ?: return@post
            val parentId = objectIds["parentId"]
            val postType = call.request.queryParameters["postType"]
            val userSession = call.sessions.get<UserSession>()!!

            val content = call.receiveParameters()["content"]
            if (content == null || commentValidator(content) != null) {
                call.response.status(HttpStatusCode.BadRequest)
                return@post
            }

            val newComment = Comment(parentId = parentId!!, content = content, authorName = userSession.name, authorId = ObjectId(userSession.id))

            if (postType.equals("task", true)) {
                taskRepository.updateCommentAmount(parentId, 1)
            } else if (postType.equals("solution", true)) {
                solutionRepository.updateCommentAmount(parentId, 1)
            } else {
                return@post
            }

            commentRepository.createComment(newComment)

            call.respondRedirect("/comments?parentId=${parentId}&postType=${postType}")
        }
    }

    delete {
        val objectIds = validateObjectIds(call, "commentId", "parentId") ?: return@delete
        val commentId = objectIds["commentId"]!!
        val authorId = commentRepository.getComment(commentId)?.authorId

        val parentId = objectIds["parentId"]!!
        val postType = call.request.queryParameters["postType"]

        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        val newCommentAmount: Int

        when (postType) {
            "task" -> {
                if (authorId == userId) {
                    newCommentAmount = taskRepository.updateCommentAmount(parentId, -1)
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
                    newCommentAmount = solutionRepository.updateCommentAmount(parentId, -1)
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
        commentRepository.deleteComment(commentId)
        call.respondHtml {
            body {
                span {
                    id = "comment-amount-${parentId}"
                    attributes["hx-swap-oob"] = "true"
                    commentCountTemplate(newCommentAmount)
                }
            }
        }
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
