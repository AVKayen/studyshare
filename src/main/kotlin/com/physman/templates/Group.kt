package com.physman.templates

import com.physman.group.GroupView
import kotlinx.html.*

fun FlowContent.groupTemplate(group: GroupView) {
    a {
        classes = setOf("group-link")
        href = "/${group.group.id}"
        span {
            classes = setOf("group")
            id = "group-${group.group.id}"
            h1 {
                +group.group.title
            }
            p {
                +(group.group.description ?: "No description")
            }
            img {
                src = group.thumbnail?.thumbnailUrl ?: "/images/group-thumbnail.png" // TODO: Add default group thumbnail?
                alt = "${group.group.title}'s thumbnail"
            }
        }
    }
}