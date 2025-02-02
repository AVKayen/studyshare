package com.physman.templates

import com.physman.task.Task
import io.ktor.http.*
import kotlinx.html.FlowContent
import kotlinx.html.*
import org.bson.types.ObjectId

fun FlowContent.taskPreviewTemplate(task: Task) {
    a(href = "/${task.groupId}/${task.id}") {
        h2 {
            classes = setOf("task-preview-title")
            +task.title
        }
    }
    cite {
        +task.authorName
    }
    task.additionalNotes?.take(100)?.let {
        p {
            +it
            if (task.additionalNotes.length >= 100) + "..."
        }
    }
    hr {
        classes = setOf("task-preview-hr")
    }
}

fun FlowContent.taskCategoryAccordion(groupId: ObjectId, taskCategory: String) {
    val taskListId = "task-list-${taskCategory.replace(' ', '-')}"

    details(classes = "task-details") {
        summary(classes = "btn outline task-category") {
            role = "button"
            attributes["hx-get"] = "/$groupId/tasks?category=${taskCategory.encodeURLParameter()}"
            attributes["hx-trigger"] = "click once"
            attributes["hx-target"] = "#$taskListId"

            h3 {
                +taskCategory
            }
        }
        div(classes = "task-list") {
            id = taskListId
        }
    }
}


