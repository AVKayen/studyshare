package com.physman.templates

import com.physman.group.GroupView
import kotlinx.html.*

fun FlowContent.groupTemplate(group: GroupView) {
    a {
        href = "/groups/${group.group.id}"
        div {
            classes = setOf("group")
            id = "group-${group.group.id}"
            h1 {
                +group.group.title
            }
            p {
                +(group.group.description ?: "No description")
            }
            img {
                src = group.thumbnail?.thumbnailUrl?.toString() ?: "/images/group-thumbnail.png" // TODO: Add default group thumbnail?
                alt = "Group thumbnail"
            }
        }
    }
}