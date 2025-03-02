package com.studyshare.routes

import com.studyshare.authentication.user.UserSession
import com.studyshare.comment.Comment
import com.studyshare.comment.CommentRepository
import com.studyshare.comment.commentValidator
import com.studyshare.group.GroupRepository
import com.studyshare.solution.SolutionRepository
import com.studyshare.task.TaskRepository
import com.studyshare.templates.commentCountTemplate
import com.studyshare.templates.commentTemplate
import com.studyshare.post.Post
import com.studyshare.utils.ResourceNotFoundException
import com.studyshare.utils.validateGroupBelonging
import com.studyshare.utils.validateRequiredObjectIds
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
        postComment(taskRepository, solutionRepository, commentRepository, groupRepository)
    }
}

fun Route.getCommentView(taskRepository: TaskRepository, solutionRepository: SolutionRepository, commentRepository: CommentRepository, groupRepository: GroupRepository) {
    get {
        val objectIds: Map<String, ObjectId> = validateRequiredObjectIds(call, "parentId") ?: return@get

        val parentId = objectIds["parentId"]
        val parentPostClassName = call.request.queryParameters["postType"]!!
        val userId = ObjectId(call.sessions.get<UserSession>()!!.id)

        val comments = commentRepository.getComments(parentId!!)
        val parentPost: Post = try {
            when (parentPostClassName.lowercase()) {
                "task" -> taskRepository.getTask(parentId).task
                "solution" -> solutionRepository.getSolution(parentId, userId).solution
                else -> return@get call.respond(HttpStatusCode.BadRequest)
            }
        } catch (e: ResourceNotFoundException) {
            call.respondText("Associated Post not found", status = HttpStatusCode.NotFound)
            return@get
        }
        if (!validateGroupBelonging(call, groupRepository, parentPost.groupId)) return@get

        call.respondHtml {
            body {
                div {
                    attributes["id"] = "comment-list-${parentId}"

                    for (comment in comments) {
                        val isAuthor = userId == comment.authorId
                        commentTemplate(comment, isAuthor, parentPostClassName)
                    }
                }
                form {
                    attributes["hx-post"] = "/comments?parentId=${parentId}&postType=${parentPostClassName}"
                    attributes["hx-target"] = "#comment-list-${parentId}"
                    attributes["hx-swap"] = "beforeend"
                    attributes["_"] = """
                        on submit
                            log "lol"
                            set targetTextarea to first <textarea/> in me
                            log targetTextarea
                            set {value: ""} on targetTextarea
                        end
                    """.trimIndent()
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

        val parentPost: Post = try {
            when (postType?.lowercase()) {
                "task" -> taskRepository.getTask(parentId!!).task
                "solution" -> solutionRepository.getSolution(parentId!!, authorId).solution
                else -> return@post call.respond(HttpStatusCode.BadRequest)
            }
        } catch (e: ResourceNotFoundException) {
            call.respondText("Associated Post not found", status = HttpStatusCode.NotFound)
            return@post
        }

        if (!validateGroupBelonging(call, groupRepository, parentPost.groupId)) return@post

        val newComment = Comment(
            parentId = parentId,
            content = content,
            authorName = userSession.name,
            authorId = authorId
        )

        try {
            when (postType.lowercase()) {
                "task" -> taskRepository.updateCommentAmount(parentId, 1)
                "solution" -> solutionRepository.updateCommentAmount(parentId, 1)
                else -> return@post
            }
        } catch (e: ResourceNotFoundException) {
            call.respondText("Associated Post not found.", status = HttpStatusCode.NotFound)
            return@post
        }

        commentRepository.createComment(newComment)

        call.respondHtml {
            body {
                commentTemplate(newComment, true, postType)
            }
        }
    }
}

fun Route.deleteComment(commentRepository: CommentRepository, solutionRepository: SolutionRepository, taskRepository: TaskRepository) {
    delete {
        val objectIds = validateRequiredObjectIds(call, "commentId", "parentId") ?: return@delete
        val commentId = objectIds["commentId"]!!
        val authorId = try {
            commentRepository.getComment(commentId).authorId
        } catch (e: ResourceNotFoundException) {
            call.respondText("Comment not found.", status = HttpStatusCode.NotFound)
            return@delete
        }

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

        val newCommentAmount = try {
            when (postType) {
                "task" -> taskRepository.updateCommentAmount(parentId, -1)
                "solution" -> solutionRepository.updateCommentAmount(parentId, -1)
                else -> return@delete call.respond(HttpStatusCode.BadRequest)
            }
        } catch (e: ResourceNotFoundException) {
            call.respondText("Associated Post not found.", status = HttpStatusCode.NotFound)
            return@delete
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