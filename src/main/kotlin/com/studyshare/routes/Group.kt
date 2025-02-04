package com.studyshare.routes

import com.studyshare.authentication.user.UserRepository
import com.studyshare.authentication.user.UserSession
import com.studyshare.forms.*
import com.studyshare.group.Group
import com.studyshare.group.GroupRepository
import com.studyshare.solution.additionalNotesValidator
import com.studyshare.solution.titleValidator
import com.studyshare.templates.*

import com.studyshare.utils.smartRedirect
import com.studyshare.utils.validateGroupBelonging
import com.studyshare.utils.validateRequiredObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId

fun Route.groupRouter(groupRepository: GroupRepository, userRepository: UserRepository) {
    val groupCreationForm = routeGroupCreationForm()
    val userAdditionForm = routeUserAdditionForm()
    route("/groups") {
        getGroupList(groupRepository, userRepository)
        postCreateGroup(groupRepository, groupCreationForm)
        route("creation-modal") {
            getGroupCreationModal(groupCreationForm)
        }
    }
    route("/{groupId}") {
        getGroupView(groupRepository)
        deleteGroup(groupRepository)
        route("/add-user") {
            getAddUserToGroupModal(userAdditionForm)
            postAddUserToGroup(groupRepository, userRepository, userAdditionForm)
        }
        route("/users-modal") {
            getUsersModal(groupRepository, userRepository)
        }
        route("/user-deletion-confirmation") {
            getUserDeletionConfirmation()
        }
        route("/group-deletion-confirmation") {
            groupDeletionConfirmation()
        }
        route("/users") {
            route ("/{userId}") {
                deleteUserFromGroup(groupRepository)
            }
        }
    }
}

fun routeGroupCreationForm(): Form {
    val groupCreationForm = Form("Create a new group", "groupForm", formAttributes = mapOf(
        "hx-swap" to "none"
    )
    )

    groupCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    groupCreationForm.addInput(TextlikeInput("Description", "description", InputType.text, additionalNotesValidator))
    groupCreationForm.addInput(FileInput("Thumbnail", "image", inputAttributes = mapOf("multiple" to "false")))

    globalFormRouter.routeFormValidators(groupCreationForm)

    return groupCreationForm
}

val nonEmptyValidator = fun(title: String): String? {
    if(title.isEmpty()) {
        return "This field cannot be empty"
    }
    return null
}

fun routeUserAdditionForm(): Form {
    val userAdditionForm = Form("Add a user to the group", "userAdditionForm", formAttributes = mapOf(
        "hx-swap" to "none"
    )
    )

    userAdditionForm.addInput(TextlikeInput("Username", "user", InputType.text, nonEmptyValidator))

    globalFormRouter.routeFormValidators(userAdditionForm)

    return userAdditionForm
}

fun Route.getGroupCreationModal(groupCreationForm: Form) {
    get {
        call.respondHtml {
            body {
                formModalDialog(
                    form = groupCreationForm,
                    callbackUrl = "/groups"
                )
            }
        }
    }
}

fun Route.getGroupList(groupRepository: GroupRepository, userRepository: UserRepository) {
    get {
        val userSession = call.sessions.get<UserSession>()!!
        val groupIds = userRepository.getUserById(userSession.id)?.groupIds ?: emptySet()
        val groupViews = groupRepository.getGroups(groupIds.toList())

        call.respondHtml {
            body {
                div {
                    classes = setOf("group-grid")
                    for (groupView in groupViews) {
                        groupThumbnailTemplate(groupView)
                    }
                }
            }
        }
    }
}

fun Route.getGroupView(groupRepository: GroupRepository) {
    get {
        validateGroupBelonging(call, groupRepository)

        val objectIds = validateRequiredObjectIds(call, "groupId") ?: return@get
        val groupId = objectIds["groupId"]!!

        val groupView = groupRepository.getGroup(groupId) ?: return@get call.respond(HttpStatusCode.NotFound)
        val userSession = call.sessions.get<UserSession>()!!

        call.respondHtml(HttpStatusCode.OK) {
            index(
                title = "StudyShare",
                username = userSession.name,
                lastBreadcrumb = groupView.group.title
            ) {
                groupViewTemplate(groupView, userSession)
            }
        }
    }
}

fun Route.getUsersModal(groupRepository: GroupRepository, userRepository: UserRepository) {
    get {
        val objectIds = validateRequiredObjectIds(call, "groupId") ?: return@get
        val groupId = objectIds["groupId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = userSession.id

        val groupView = groupRepository.getGroup(groupId) ?: return@get call.respond(HttpStatusCode.NotFound)

        val groupMembers = userRepository.getUsersByIds(groupView.group.memberIds)
        val groupLeader = groupMembers.first { it.id == groupView.group.leaderId }
        val remainingMembers = groupMembers.filter { it.id != groupView.group.leaderId }

        call.respondHtml {
            body {
                modalTemplate(
                    title = "Group members",
                    secondaryButtonText = null
                ) {
                    userListItem(groupLeader, groupId, false)
                    remainingMembers.forEach {
                        userListItem(it, groupId, userId == groupLeader.id.toHexString())
                    }
                }
            }
        }
    }
}

fun Route.postCreateGroup(groupRepository: GroupRepository, groupCreationForm: Form) {
    post {
        val formSubmissionData = groupCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val description = formSubmissionData.fields["description"]!!
        val image = formSubmissionData.files.firstOrNull()

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        val group = Group(
            title = title,
            description = description,
            leaderId = userId,
            memberIds = listOf(userId),
            thumbnailId = null
        )

        groupRepository.createGroup(
            group = group,
            groupThumbnailFile = image
        )

        formSubmissionData.cleanup()
        call.smartRedirect(redirectUrl = "/${group.id}")
    }
}

fun Route.postAddUserToGroup(groupRepository: GroupRepository, userRepository: UserRepository, userAdditionForm: Form) {
    post {
        val formSubmissionData = userAdditionForm.validateSubmission(call) ?: return@post
        val user = formSubmissionData.fields["user"]!!
        val objectIds = validateRequiredObjectIds(call, "groupId") ?: return@post
        val groupId = objectIds["groupId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = userSession.id

        if(!groupRepository.isUserGroupLeader(groupId, ObjectId(userId))) {
            call.respondText(
                text = "You must be a group leader to add new users to the group.",
                status = HttpStatusCode.Forbidden
            )
            return@post
        }

        val userToAdd = userRepository.getUserByName(user)
        if (userToAdd == null) {
            userAdditionForm.respondFormError(call, "User \"$user\" not found")
            return@post
        }
        if (groupRepository.isUserMember(groupId, userToAdd.id)) {
            userAdditionForm.respondFormError(call, "User \"$user\" is already a member of this group")
            return@post
        }
        groupRepository.addUser(groupId, userToAdd.id)

        call.respondRedirect("/${groupId}")
    }
}

fun Route.getAddUserToGroupModal(userAdditionForm: Form) {
    get {
        val objectIds = validateRequiredObjectIds(call, "groupId") ?: return@get
        val groupId = objectIds["groupId"]!!

        call.respondHtml {
            body {
                formModalDialog(
                    form = userAdditionForm,
                    callbackUrl = "/${groupId}/add-user"
                )
            }
        }
    }
}

fun Route.getUserDeletionConfirmation() {
    get {
        val objectIds = validateRequiredObjectIds(call, "groupId", "userId") ?: return@get
        val groupId = objectIds["groupId"]!!
        val userId = objectIds["userId"]!!

        val name = call.request.queryParameters["name"] ?: "user"

        call.respondHtml {
            body {
                userDeletionConfirmation(
                    groupId = groupId.toHexString(), userId = userId.toHexString(), name = name
                )
            }
        }
    }
}

fun Route.deleteUserFromGroup(groupRepository: GroupRepository) {
    delete {
        val objectIds = validateRequiredObjectIds(call, "groupId", "userId") ?: return@delete
        val groupId = objectIds["groupId"]!!
        val targetUserId = objectIds["userId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        val groupView = groupRepository.getGroup(groupId) ?: return@delete call.respond(HttpStatusCode.NotFound)
        if (!groupView.group.canUserKick(userId)) {
            call.respond(HttpStatusCode.Forbidden)
            return@delete
        }

        groupRepository.deleteUser(groupId, targetUserId)

        call.respondHtml { body() }
    }
}

fun Route.groupDeletionConfirmation() {
    get {
        val objectIds = validateRequiredObjectIds(call, "groupId") ?: return@get
        val groupId = objectIds["groupId"]!!

        val groupTitle = call.request.queryParameters["groupTitle"] ?: return@get call.respondText(
            text = "No groupTitle specified.", status = HttpStatusCode.BadRequest
        )

        call.respondHtml {
            body {
                confirmationModalTemplate(
                    title = "Delete the group?",
                    details = "Are you sure you want to delete the group \"${groupTitle}\"?",
                    submitText = "Delete",
                    submitAttributes = mapOf(
                        "hx-delete" to "/$groupId"
                    )
                )
            }
        }
    }
}

fun Route.deleteGroup(groupRepository: GroupRepository) {
    delete {
        val objectIds = validateRequiredObjectIds(call, "groupId") ?: return@delete
        val groupId = objectIds["groupId"]!!

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        val groupView = groupRepository.getGroup(groupId) ?: return@delete call.respond(HttpStatusCode.NotFound)
        if (groupView.group.leaderId != userId) {
            call.respond(HttpStatusCode.Forbidden)
            return@delete
        }

        groupRepository.deleteGroup(groupId)

        call.smartRedirect("/")
    }
}