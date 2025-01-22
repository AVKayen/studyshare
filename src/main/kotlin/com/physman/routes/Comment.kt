package com.physman.routes

import com.physman.comment.Comment
import com.physman.comment.CommentRepository
import com.physman.comment.commentValidator
import com.physman.forms.*
import com.physman.solution.SolutionRepository
import com.physman.task.TaskRepository
import com.physman.templates.commentTemplate
import com.physman.templates.index
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.html.*
import org.bson.types.ObjectId


fun Route.commentRouter(commentRepository: CommentRepository, solutionRepository: SolutionRepository, taskRepository: TaskRepository) {
    val commentCreationForm = Form("Create a new comment", "commentForm", formAttributes = mapOf(
            "hx-swap" to "none" // because currently this form is on an empty page
        ))
    commentCreationForm.addInput(TextlikeInput("content", "content", InputType.text, commentValidator))

    globalFormRouter.routeFormValidators(commentCreationForm)

    //TODO: redo these
    get("/comment") {
        val parentId = call.request.queryParameters["parentId"]
        val postType = call.request.queryParameters["post-type"]
        if (parentId == null) {
                call.respondText("No id specified.", status = HttpStatusCode.BadRequest)
                return@get
        }

        call.respondHtml {
            index("This won't be index") {
                //TODO: maybe separate tasks from solutions
                commentCreationForm.render(this, call.url())
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

    route("/{post-type}")
    {
        post { //todo: get rid of those prints
            println("EOOOOOOOOO")
            val objectIds: Map<String, ObjectId> = validateObjectIds(call, "parentId") ?: return@post
            val parentId = objectIds["parentId"]
            val postType = call.request.queryParameters["post-type"]
            println("EOOOOOOOOO2")
            val formSubmissionData: FormSubmissionData = commentCreationForm.validateSubmission(call) ?: return@post
            val content = formSubmissionData.fields["content"]!!
            println("EOOOOOOOOO3")
            val newComment = Comment(parentId = parentId!!, content = content)

            if (postType.equals("task", true)){
                taskRepository.updateCommentAmount(parentId, 1)
            } else if (postType.equals("solution", true)) {
                solutionRepository.updateCommentAmount(parentId, 1)
            } else {
                println("EOOOOOOOOO3.5")
                println(call.url())
                if (postType!=null)
                    println(postType + postType.javaClass)
                return@post
            }
            println("EOOOOOOOOO4")
            commentRepository.createComment(newComment)

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
                val postType = call.request.queryParameters["post-type"]

                if (postType.equals("task", true)){
                    taskRepository.updateCommentAmount(parentId, 1)
                } else if (postType.equals("task", true)) {
                    solutionRepository.updateCommentAmount(parentId, 1)
                } else {
                    return@delete
                }

                commentRepository.deleteComment(commentId)

                call.response.status(HttpStatusCode.NoContent)
            }
        }
    }


}