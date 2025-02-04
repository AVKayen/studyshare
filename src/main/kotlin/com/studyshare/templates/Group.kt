package com.studyshare.templates

import com.studyshare.authentication.user.User
import com.studyshare.authentication.user.UserSession
import com.studyshare.group.GroupView
import kotlinx.html.*
import org.bson.types.ObjectId

fun FlowContent.groupThumbnailTemplate(groupView: GroupView) {
    a {
        classes = setOf("group-link")
        href = "/${groupView.group.id}"
        span {
            classes = setOf("group")
            id = "group-${groupView.group.id}"
            div(classes = "group-thumbnail-header") {
                h3 {
                    +groupView.group.title
                }
                p {
                    +(groupView.group.description ?: "No description")
                }
            }
            groupView.thumbnail?.let {
                img(src = it.thumbnailUrl, alt = "${groupView.group.title}'s thumbnail")
            }
        }
    }
}

fun FlowContent.groupViewTemplate(groupView: GroupView, userSession: UserSession) {

    val groupId = groupView.group.id

    div(classes = "group-header") {
        div(classes = "group-info") {
            groupView.thumbnail?.let {
                div {
                    classes = setOf("group-thumbnail")
                    img(src = it.thumbnailUrl, alt = "${groupView.group.title}'s thumbnail")
                }
            }
            div {
                classes = setOf("group-info-text")
                h1 {
                    +groupView.group.title
                }
                groupView.group.description?.let {
                    p { +it }
                }
            }
        }
        div(classes = "group-options") {
            if (groupView.group.leaderId == ObjectId(userSession.id)) {
                deletionButton(
                    getUrl = "/$groupId/group-deletion-confirmation?groupTitle=${groupView.group.title}"
                )
            }
        }
    }
    section(classes = "wide-button-container") {
        modalOpenButton(
            buttonText = "Create a task",
            modalUrl = "/${groupId}/creation-modal"
        )
        modalOpenButton(
            buttonText = "Show members",
            modalUrl = "/${groupId}/users-modal"
        )
        if (userSession.id == groupView.group.leaderId.toHexString()) {
            modalOpenButton(
                buttonText = "Add a user",
                modalUrl = "/${groupId}/add-user"
            )
        }
    }

    groupView.group.taskCategories.reversed().forEach {
        taskCategoryAccordion(groupId, it)
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
            attributes["hx-delete"] = "/$groupId/users/$userId"
            attributes["hx-target"] = "#user-list-item-${userId}"
            attributes["hx-swap"] = "delete"
            attributes["_"] = """
                on click
                    remove #$confirmationId
                    remove #$userListHrId
                end
            """.trimIndent()
            +"Kick"
        }
    }
}

