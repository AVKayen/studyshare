package com.physman.routes

import com.physman.authentication.user.UserSession
import com.physman.comment.Comment
import com.physman.comment.CommentRepository
import com.physman.comment.commentValidator
import com.physman.group.GroupRepository
import com.physman.solution.SolutionRepository
import com.physman.task.TaskRepository
import com.physman.templates.commentCountTemplate
import com.physman.templates.commentTemplate
import com.physman.utils.Post
import com.physman.utils.validateGroupBelonging
import com.physman.utils.validateRequiredObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId

fun Route.commentRouter(commentRepository: CommentRepository, solutionRepository: SolutionRepository, taskRepository: TaskRepository, groupRepository: GroupRepository) {
    route("/comments") {
        getCommentView(taskRepository, solutionRepository, commentRepository, groupRepository)
        deleteComment(commentRepository, solutionRepository, taskRepository)
        route("/comment") {
            postComment(taskRepository, solutionRepository, commentRepository, groupRepository)
        }
    }
}

fun Route.getCommentView(taskRepository: TaskRepository, solutionRepository: SolutionRepository, commentRepository: CommentRepository, groupRepository: GroupRepository) {
    get {
        val objectIds: Map<String, ObjectId> = validateRequiredObjectIds(call, "parentId") ?: return@get

        val parentId = objectIds["parentId"]
        val parentPostClassName = call.request.queryParameters["postType"]!!
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        val comments = commentRepository.getComments(parentId!!)
        val parentPost = when (parentPostClassName.lowercase()) {
            "task" -> taskRepository.getTask(parentId)?.task
            "solution" -> solutionRepository.getSolution(parentId, userId)?.solution
            else -> return@get
        }
        if (!validateGroupBelonging(call, groupRepository, parentPost?.groupId)) return@get

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
}

fun Route.postComment(taskRepository: TaskRepository, solutionRepository: SolutionRepository, commentRepository: CommentRepository, groupRepository: GroupRepository) {
    post {
        val objectIds: Map<String, ObjectId> = validateRequiredObjectIds(call, "parentId") ?: return@post
        val parentId = objectIds["parentId"]
        val postType = call.request.queryParameters["postType"]
        val userSession = call.sessions.get<UserSession>()!!
        val authorId = ObjectId(userSession.id)

        val content = call.receiveParameters()["content"]
        if (content == null || commentValidator(content) != null) {
            call.response.status(HttpStatusCode.BadRequest)
            return@post
        }

        val parentPost: Post? = when (postType?.lowercase()) {
            "task" -> taskRepository.getTask(parentId!!)?.task
            "solution" -> solutionRepository.getSolution(parentId!!, authorId)?.solution
            else -> return@post
        }

        if (parentPost == null) {
            call.respond(HttpStatusCode.NotFound)
            return@post
        }

        if (!validateGroupBelonging(call, groupRepository, parentPost.groupId)) return@post

        val newComment = Comment(
            parentId = parentId,
            content = content,
            authorName = userSession.name,
            authorId = authorId
        )

        when (postType.lowercase()) {
            "task" -> taskRepository.updateCommentAmount(parentId, 1)
            "solution" -> solutionRepository.updateCommentAmount(parentId, 1)
            else -> return@post
        }

        commentRepository.createComment(newComment)

        call.respondRedirect("/comments?parentId=${parentId}&postType=${postType}")
    }
}

fun Route.deleteComment(commentRepository: CommentRepository, solutionRepository: SolutionRepository, taskRepository: TaskRepository) {
    delete {
        val objectIds = validateRequiredObjectIds(call, "commentId", "parentId") ?: return@delete
        val commentId = objectIds["commentId"]!!
        val authorId = commentRepository.getComment(commentId)?.authorId

        val parentId = objectIds["parentId"]!!
        val postType = call.request.queryParameters["postType"]

        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        if (authorId != userId) {
            call.respondText(
                "Resource Modification Restricted - Ownership Required",
                status = HttpStatusCode.Forbidden
            )
            return@delete
        }

        val newCommentAmount = when (postType) {
            "task" -> taskRepository.updateCommentAmount(parentId, -1)
            "solution" -> solutionRepository.updateCommentAmount(parentId, -1)
            else -> return@delete
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