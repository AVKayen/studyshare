package com.physman.routes

import com.physman.comment.Comment
import com.physman.comment.CommentRepository
import com.physman.comment.commentValidator
import com.physman.forms.*
import com.physman.solution.Solution
import com.physman.templates.commentTemplate
import com.physman.templates.index
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId


fun Route.commentRouter(commentRepository: CommentRepository) {
    val commentCreationForm = Form("Create a new comment", "commentForm", mapOf(
            "hx-swap" to "none" // because currently this form is on an empty page
        ))
    commentCreationForm.addInput(TextlikeInput("content", "content", InputType.text, commentValidator))

    globalFormRouter.routeFormValidators(commentCreationForm)

    get("/comment") {
        var taskId = call.request.queryParameters["taskId"]
        if (taskId == null) {
            val solutionId = call.request.queryParameters["solutionId"]
            if (solutionId == null) {
                call.respondText("No id specified.", status = HttpStatusCode.BadRequest)
                return@get
            }
            taskId = commentRepository.getTaskIdFromSolution(ObjectId(solutionId)).toString()
        }

        call.respondHtml {
            index("This won't be index") {
                commentCreationForm.render(this, "/solutions?taskId=$taskId")
            }
        }
    }

    get {

        var parentStrId = validateObjectIds(call, "taskId")
        var parentId = parentStrId?.get("taskId")
        if (parentId == null) {
            parentStrId = validateObjectIds(call, "solutionId")
            parentId = parentStrId?.get("solutionId")
            if (parentId == null) return@get
        }

        val comments = commentRepository.getComments(parentId)

        call.respondHtml {
            body {
                for (comment in comments) {
                    commentTemplate(comment)
                }
            }
        }

    }

    post {
        var parentStrId = validateObjectIds(call, "taskId")
        var parentId = parentStrId?.get("taskId")
        if (parentId == null) {
            parentStrId = validateObjectIds(call, "solutionId")
            parentId = parentStrId?.get("solutionId")
            if (parentId == null) return@post
        }

        val formSubmissionData: FormSubmissionData = commentCreationForm.validateSubmission(call) ?: return@post
        val content = formSubmissionData.fields["content"]!!

        val newComment = Comment(parentId = parentId!!, content = content)

        commentRepository.createComment(newComment)

        call.respondHtml(HttpStatusCode.OK) {
            body {
//                solutionTemplate(newSolution, taskId.toString())
            }
        }
    }

    route("/{id}") {
        delete {
            val objectIds = validateObjectIds(call, "id") ?: return@delete
            val commentId = objectIds["id"]!!

            commentRepository.deleteComment(commentId)
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}