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
    val userHrId = "user-list-item-hr-${user.id}"
    div(classes = "user-list-item") {
        attributes["id"] = userItemId
        p {
            +user.name
        }
        if (showKickButton) {
            button(classes = "btn secondary outline") {
                attributes["_"] = "on click add .invisible to me toggle @disabled on me"
                attributes["hx-get"] = "/$groupId/user-deletion-confirmation?userId=${user.id}&name=${user.name}"
                attributes["hx-target"] = "#$userItemId"
                attributes["hx-swap"] = "afterend"

                span(classes = "material-symbols-rounded") {
                    +"delete"
                }
            }
        }
    }
    hr {
        attributes["id"] = userHrId
        classes = setOf("user-list-item-hr")
    }
}

fun FlowContent.userDeletionConfirmation(groupId: String, userId: String, name: String) {

    val userListItemId = "user-list-item-$userId"
    val userListHrId = "user-list-item-hr-$userId"
    val confirmationId = "user-deletion-confirmation-${userId}"

    div(classes = "user-deletion-confirmation") {
        attributes["id"] = confirmationId
        small {
            +"Are you sure you want to kick $name from this group?"
        }
        button(classes = "secondary outline") {
            attributes["_"] = """
                on click
                    remove #$confirmationId
                    set parentUserListItem to <button/> in #$userListItemId
                    remove .invisible from parentUserListItem
                    toggle @disabled on parentUserListItem
                    remove .invisible from
                end
            """.trimIndent()

            +"Cancel"
        }
        button(classes = "secondary") {
            attributes["_"] = "on click remove #$confirmationId"
            attributes["hx-delete"] = "/$groupId/users/$userId"
            attributes["hx-target"] = "#user-list-item-${userId}"
            attributes["hx-swap"] = "delete"
            attributes["_"] = """
                on click
                    remove #$confirmationId
                end
            """.trimIndent()
            +"Kick"
        }
    }
}

