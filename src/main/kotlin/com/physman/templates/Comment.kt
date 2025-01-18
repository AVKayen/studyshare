package com.physman.templates

import com.physman.comment.Comment
import com.physman.task.TaskView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.commentTemplate(comment: Comment) {
    //TODO: beautify it ;)
    //also, there is a bit of htmx in task, solution and comment routes, you might want to change it

            h6 {
                +"Comment:".plus(comment.content)
            }


}

