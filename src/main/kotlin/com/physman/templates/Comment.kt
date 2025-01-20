package com.physman.templates

import com.physman.comment.Comment
import kotlinx.html.FlowContent
import kotlinx.html.*
import org.bson.types.ObjectId

fun FlowContent.commentTemplate(comment: Comment) {
    //TODO: beautify it ;)
    //also, there is a bit of htmx in task, solution and comment routes, you might want to change it
    //there are comment examples in testTask task
            h6 {
                +"> ".plus(comment.content)
            }


}

fun FlowContent.showCommentsButton(parentId: ObjectId) {
    button {
        classes = setOf("btn")
        attributes["hx-get"] = "/comments?parentId=${parentId}"
        attributes["hx-trigger"] = "click"
        attributes["hx-target"] = "#comments-${parentId}"
        +"Show comments (commentCount())"
    }
}