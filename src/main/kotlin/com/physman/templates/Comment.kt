package com.physman.templates

import com.physman.comment.Comment
import com.physman.task.TaskView
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.commentTemplate(comment: Comment) {
    //TODO: beautify it ;)
    article(classes = "flex-col task") {
            h2 {
                +comment.content
            }

    }
}

