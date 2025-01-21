package com.physman.routes

import com.physman.comment.Comment
import com.physman.comment.CommentRepository
import com.physman.comment.commentValidator
import com.physman.forms.*
import com.physman.solution.SolutionRepository
import com.physman.templates.commentTemplate
import com.physman.templates.index
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.bson.types.ObjectId


fun Route.commentRouter(commentRepository: CommentRepository, solutionRepository: SolutionRepository) {
    val commentCreationForm = Form("Create a new comment", "commentForm", formAttributes = mapOf(
            "hx-swap" to "none" // because currently this form is on an empty page
        ))
    commentCreationForm.addInput(TextlikeInput("content", "content", InputType.text, commentValidator))

    globalFormRouter.routeFormValidators(commentCreationForm)

    get("/comment") {
        val parentId = call.request.queryParameters["parentId"]
        if (parentId == null) {
                call.respondText("No id specified.", status = HttpStatusCode.BadRequest)
                return@get
        }

        call.respondHtml {
            index("This won't be index") {
                //TODO: maybe separate tasks from solutions
                commentCreationForm.render(this, "/comments?parentId=$parentId")
            }
        }
    }

    get {

        val objectIds: Map<String, ObjectId> = validateObjectIds(call, "parentId") ?: return@get
        val parentId = objectIds["parentId"]

        val comments = commentRepository.getComments(parentId!!)

        call.respondHtml {
            body {
                for (comment in comments) {
                    commentTemplate(comment)
                }
            }
        }

    }

    post {
        val objectIds: Map<String, ObjectId> = validateObjectIds(call, "parentId") ?: return@post
        val parentId = objectIds["parentId"]

        val formSubmissionData: FormSubmissionData = commentCreationForm.validateSubmission(call) ?: return@post
        val content = formSubmissionData.fields["content"]!!

        val newComment = Comment(parentId = parentId!!, content = content)

        commentRepository.createComment(newComment)
        solutionRepository.updateCommentAmount(parentId, 1)

        call.respondHtml(HttpStatusCode.OK) {
            body {
//                solutionTemplate(newSolution, taskId.toString())
            }
        }
    }

    route("/{comment-id}") {
        delete {
            val objectIds = validateObjectIds(call, "comment-id", "parentId") ?: return@delete
            val commentId = objectIds["comment-id"]!!
            val parentId = objectIds["parentId"]!!

            commentRepository.deleteComment(commentId)
            solutionRepository.updateCommentAmount(parentId, -1)
            call.response.status(HttpStatusCode.NoContent)
        }
    }
}