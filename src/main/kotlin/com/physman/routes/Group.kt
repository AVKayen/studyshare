package com.physman.routes

import com.physman.authentication.user.UserRepository
import com.physman.authentication.user.UserSession
import com.physman.forms.FileInput
import com.physman.forms.Form
import com.physman.forms.TextlikeInput
import com.physman.forms.globalFormRouter
import com.physman.group.Group
import com.physman.group.GroupRepository
import com.physman.group.GroupView
import com.physman.solution.additionalNotesValidator
import com.physman.solution.titleValidator
import com.physman.templates.formModalDialog
import com.physman.templates.formModalOpenButton
import com.physman.templates.groupTemplate
import com.physman.templates.index
import com.physman.utils.validateObjectIds
import io.ktor.http.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*
import org.bson.types.ObjectId

fun Route.groupRouter(groupRepository: GroupRepository, userRepository: UserRepository) {
    val groupCreationForm = Form("Create a new group", "groupForm", formAttributes = mapOf(
        "hx-swap" to "none"
    ))


    groupCreationForm.addInput(TextlikeInput("Title", "title", InputType.text, titleValidator))
    groupCreationForm.addInput(TextlikeInput("Description", "description", InputType.text, additionalNotesValidator))
    groupCreationForm.addInput(FileInput("Thumbnail", "image", inputAttributes = mapOf("multiple" to "true")))

    globalFormRouter.routeFormValidators(groupCreationForm)

    get {
        val userSession = call.sessions.get<UserSession>()!!
        val groups = userRepository.getUserById(userSession.id)?.groupIds ?: emptySet()
        val groupViews = mutableListOf<GroupView>()
        // TODO: Create something like getGroupsByIds
        for (group in groups) {
            groupRepository.getGroup(group)?.let { groupViews.add(it) }
        }
        call.respondHtml {
            body {
                classes = setOf("group-grid")
                for (groupView in groupViews) {
                    groupTemplate(groupView)
                }
            }
        }
    }

    post {
        val formSubmissionData = groupCreationForm.validateSubmission(call) ?: return@post
        val title = formSubmissionData.fields["title"]!!
        val description = formSubmissionData.fields["description"]!!
        val image = formSubmissionData.files.firstOrNull()

        val userSession = call.sessions.get<UserSession>()!!
        val userId = ObjectId(userSession.id)

        groupRepository.createGroup(
            Group(
                title = title,
                description = description,
                leaderId = userId,
                memberIds = listOf(userId),
                thumbnailId = null
            ),
            groupThumbnailFile = image
        )

        call.respondRedirect("/")
    }

    get("/creation-modal") {
        call.respondHtml {
            body {
                formModalDialog(
                    form = groupCreationForm,
                    callbackUrl = "/group"
                )
            }
        }
    }


}