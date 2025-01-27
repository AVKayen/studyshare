package com.physman.templates

import com.physman.comment.Comment
import com.physman.solution.Solution
import com.physman.task.Task
import com.physman.utils.Post
import com.physman.utils.className
import com.physman.utils.objectIdToSimpleDateString
import io.ktor.server.util.*
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.commentTemplate(comment: Comment, isAuthor: Boolean) {
    div {
        classes = setOf("comment")
        cite {
            +"${comment.authorName} @ ${objectIdToSimpleDateString(comment.id)}"
        }
        h5 {
            +comment.content
        }
        if(isAuthor) {
            commentDeletionButton(comment)
        }
    }
}

fun FlowContent.showCommentsAccordion(parentPost: Post) {
    details {
        summary {
            role = "button"
            classes = setOf("btn comment-button outline")
            attributes["hx-get"] = "/comments?parentId=${parentPost.id}&post-type=${className(parentPost)}"
            attributes["hx-trigger"] = "click once"
            attributes["hx-target"] = "#comments-${parentPost.id}"
            span {
                classes = setOf("material-symbols-rounded", "comment-icon")
                +"comment"
            }
            span {
                id = "comment-amount-${parentPost.id}"
                commentCountTemplate(parentPost.commentAmount)
            }
        }
        div {
            id = "comments-${parentPost.id}"
            classes = setOf("comments")
        }
    }

}

fun FlowContent.commentCountTemplate(commentAmount: Int) {
    if (commentAmount == 0) {
        +"Be the first to comment"
    } else if (commentAmount == 1) {
        +"Show 1 comment"
    } else {
        +"Show $commentAmount comments"
    }
}

fun FlowContent.commentDeletionButton(comment: Comment) {
    val url = "/comments?comment-id=${comment.id}"

    button(classes = "btn secondary outline") {
        attributes["hx-delete"] = url
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        span(classes = "material-symbols-rounded") {
            +"delete"
        }
    }
}