package com.physman.templates

import com.physman.comment.Comment
import com.physman.utils.Post
import com.physman.utils.className
import com.physman.utils.objectIdToSimpleDateString
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.commentTemplate(comment: Comment, isAuthor: Boolean, postType: String) {
    div {
        id = "comment-${comment.id.toHexString()}"
        classes = setOf("comment")
        cite {
            +"${comment.authorName} @ ${objectIdToSimpleDateString(comment.id)}"
        }
        h5 {
            +comment.content
        }
        if(isAuthor) {
            commentDeletionButton(comment, postType)
        }
    }
}

fun FlowContent.showCommentsAccordion(parentPost: Post) {
    details {
        summary {
            role = "button"
            classes = setOf("btn comment-button outline")
            attributes["hx-get"] = "/comments?parentId=${parentPost.id}&postType=${className(parentPost)}"
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
    when (commentAmount) {
        0 -> +"Be the first to comment"
        1 -> +"Show 1 comment"
        else -> +"Show $commentAmount comments"
    }
}

fun FlowContent.commentDeletionButton(comment: Comment, postType: String) {
    val url = "/comments?commentId=${comment.id}&postType=${postType}&parentId=${comment.parentId}"

    button(classes = "btn secondary outline") {
        attributes["hx-delete"] = url
        attributes["hx-target"] = "#comment-${comment.id.toHexString()}"
        attributes["hx-swap"] = "outerHTML"

        span(classes = "material-symbols-rounded") {
            +"delete"
        }
    }
}