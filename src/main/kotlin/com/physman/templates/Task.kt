package com.physman.templates

import com.physman.task.Task
import kotlinx.html.FlowContent
import kotlinx.html.*

fun FlowContent.taskTemplate(task: Task) {
    div {
        div {
            h2 {
                +task.title
            }
        }
        div {
            if(task.additionalNotes != null) {
                +"Notes: ${task.additionalNotes}"
            }
        }
    }
}
