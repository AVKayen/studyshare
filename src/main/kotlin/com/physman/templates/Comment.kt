package com.physman.templates

import com.physman.comment.Comment
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.commentTemplate(comment: Comment) {
    //TODO: beautify it ;)
    //also, there is a bit of htmx in task, solution and comment routes, you might want to change it
    //there are comment examples in testTask task
            h6 {
                +"Comment:".plus(comment.content)
            }


}

