package com.physman.templates

import com.physman.comment.Comment
import com.physman.utils.Post
import com.physman.utils.className
import com.physman.utils.objectIdToSimpleDateString
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.commentTemplate(comment: Comment, isAuthor: Boolean, postType: String) {
    div {
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
            attributes["hx-get"] = "/comments?parent-id=${parentPost.id}&post-type=${className(parentPost)}"
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

fun FlowContent.commentDeletionButton(comment: Comment, postType: String) {
    val url = "/comments?comment-id=${comment.id}&post-type=${postType}&parent-id=${comment.parentId}"

    button(classes = "btn secondary outline") {
        attributes["hx-delete"] = url
        attributes["hx-target"] = "body"
        attributes["hx-swap"] = "beforeend"

        span(classes = "material-symbols-rounded") {
            +"delete"
        }
    }
}