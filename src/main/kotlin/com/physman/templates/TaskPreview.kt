package com.physman.templates

import com.physman.task.Task
import io.ktor.http.*
import kotlinx.html.FlowContent
import kotlinx.html.*
import org.bson.types.ObjectId

fun FlowContent.taskPreviewTemplate(task: Task) {
    article(classes = "flex-col task") {
        a(href = "/${task.groupId}/${task.id}") {
            h4 {
                +task.title
            }
        }
    }
}

fun FlowContent.taskCategoryAccordion(groupId: ObjectId, taskCategory: String) {

    val taskListId = "task-list-${taskCategory.replace(' ', '-')}"

    details(classes = "task-details") {
        summary(classes = "btn secondary task-category") {
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

