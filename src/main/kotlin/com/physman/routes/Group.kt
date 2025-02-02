package com.physman.routes

import com.physman.authentication.user.UserRepository
import com.physman.authentication.user.UserSession
import com.physman.forms.*
import com.physman.group.Group
import com.physman.group.GroupRepository
import com.physman.solution.additionalNotesValidator
import com.physman.solution.titleValidator
import com.physman.templates.*

import com.physman.utils.smartRedirect
import com.physman.utils.validateGroupBelonging
import com.physman.utils.validateRequiredObjectIds
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
        route("/add-user") {
            getAddUserToGroupModal(userAdditionForm)
            postAddUserToGroup(groupRepository, userRepository, userAdditionForm)
        }
        route("/users-modal") {
            getUsersModal(groupRepository, userRepository)
        }
    }
}

fun routeGroupCreationForm(): Form {
    val groupCreationForm = Form("Create a new group", "groupForm", formAttributes = mapOf(
        "hx-swap" to "none"
    ))

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
    ))

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
                        groupTemplate(groupView)
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

                groupView.group.taskCategories.forEach {
                    taskCategoryAccordion(groupId, it)
                }
            }
        }
    }
}

fun Route.getUsersModal(groupRepository: GroupRepository, userRepository: UserRepository) {
    get {
        val objectIds = validateRequiredObjectIds(call, "groupId") ?: return@get
        val groupId = objectIds["groupId"]!!

        val groupView = groupRepository.getGroup(groupId) ?: return@get call.respond(HttpStatusCode.NotFound)

        val groupMembers = userRepository.getUsersByIds(groupView.group.memberIds)

        call.respondHtml {
            body {
                modalTemplate(
                    title = "Group members",
                    secondaryButtonText = null
                ) {
                    groupMembers.forEach {
                        p(classes = "user-list-item") {
                            +it.name
                        }
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

        val userToAdd = userRepository.getUserByName(user)
        if (userToAdd == null) {
            userAdditionForm.respondFormError(call, "User \"$user\" not found")
            return@post
        }
        if (groupRepository.isUserMember(groupId, ObjectId(userToAdd.id))) {
            userAdditionForm.respondFormError(call, "User \"$user\" is already a member of this group")
            return@post
        }
        groupRepository.addUser(groupId, ObjectId(userToAdd.id))

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