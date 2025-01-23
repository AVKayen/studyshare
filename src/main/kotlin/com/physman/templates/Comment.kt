package com.physman.templates

import com.physman.comment.Comment
import com.physman.utils.Post
import com.physman.utils.className
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.commentTemplate(comment: Comment) {
    div {
        classes = setOf("comment")
        h6 {
            +comment.authorName
        }
        h5 {
            +comment.content
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