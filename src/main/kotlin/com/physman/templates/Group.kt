package com.physman.templates

import com.physman.authentication.user.User
import com.physman.group.GroupView
import kotlinx.html.*
import org.bson.types.ObjectId

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

fun FlowContent.userListItem(user: User, groupId: ObjectId, showKickButton: Boolean) {
    val userItemId = "user-list-item-${user.id}"
    div(classes = "user-list-item") {
        attributes["id"] = userItemId
        p {
            +user.name
        }
        if (showKickButton) {
            button(classes = "btn secondary outline") {
                attributes["hx-get"] = "/$groupId/user-deletion-confirmation?userId=${user.id}&name=${user.name}"
                attributes["hx-target"] = "#$userItemId"
                attributes["hx-swap"] = "afterend"

                span(classes = "material-symbols-rounded") {
                    +"delete"
                }
            }
        }
    }
}

fun FlowContent.userDeletionConfirmation(groupId: String, userId: String, name: String) {
    val confirmationId = "user-deletion-confirmation-${userId}"

    div(classes = "user-deletion-confirmation") {
        attributes["id"] = confirmationId
        p {
            +"Are you sure you want to kick $name from this group?"
        }
        button(classes = "secondary outline") {
            attributes["_"] = "on click remove #$confirmationId"
            +"Cancel"
        }
        button(classes = "secondary") {
            attributes["_"] = "on click remove #$confirmationId"
            attributes["hx-delete"] = "/$groupId/users/$userId"
            attributes["hx-target"] = "#user-list-item-${userId}"
            attributes["hx-swap"] = "delete"
            +"Kick"
        }
    }
}

