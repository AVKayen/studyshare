package com.physman.templates

import com.physman.comment.Comment
import com.physman.utils.Post
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.commentTemplate(comment: Comment) {
    //TODO: beautify it ;)
    //also, there is a bit of htmx in task, solution and comment routes, you might want to change it
    //there are comment examples in testTask task
            h6 {
                +"> ".plus(comment.content)
            }


}

fun FlowContent.showCommentsButton(parentPost: Post) {
    button {
        classes = setOf("btn comment-button outline")
        attributes["hx-get"] = "/comments?parentId=${parentPost.id}&post-type=${strip(parentPost.javaClass.toString())}"
        attributes["hx-trigger"] = "click"
        attributes["hx-target"] = "#comments-${parentPost.id}"
        span {
            classes = setOf("material-symbols-rounded", "comment-icon")
            +"comment"
        }
        +"Show ${parentPost.commentAmount} comments"

        a(href = "/comments/comment?parentId=${parentPost.id}&post-type=${strip(parentPost.javaClass.toString())}") {
            +"+"}
    }
}

//TODO: move this somewhere
fun strip(str: String): String {
    val lastDotIndex = str.lastIndexOf('.')
    return if (lastDotIndex != -1) {
        str.substring(lastDotIndex + 1)
    } else {
        str
    }
}