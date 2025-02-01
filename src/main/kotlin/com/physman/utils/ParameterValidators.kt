package com.physman.utils

import com.physman.authentication.user.UserSession
import com.physman.group.GroupRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.bson.types.ObjectId

suspend fun validateRequiredObjectIds(call: RoutingCall, vararg parameterNames: String): Map<String, ObjectId>? {

    val validatedObjectIds = mutableMapOf<String, ObjectId>()
    parameterNames.forEach { parameterName: String ->
        val parameter: String? = call.parameters[parameterName] ?: call.request.queryParameters[parameterName]
        if (parameter == null) {
            call.respondText(
                text = "Invalid value for $parameterName parameter: it can not be null",
                status = HttpStatusCode.BadRequest
            )
            return null
        }
        if (!ObjectId.isValid(parameter)) {
            call.respondText(
                text = "Invalid value for $parameterName parameter: $parameter is not a valid ObjectId",
                status = HttpStatusCode.BadRequest
            )
            return null
        }
        validatedObjectIds[parameterName] = ObjectId(parameter)
    }

    return validatedObjectIds
}

suspend fun validateOptionalObjectIds(call: RoutingCall, vararg parameterNames: String): Map<String, ObjectId?>? {
    val validatedObjectIds = mutableMapOf<String, ObjectId?>()
    parameterNames.forEach { parameterName: String ->
        val parameter: String? = call.parameters[parameterName] ?: call.request.queryParameters[parameterName]
        if (parameter == null) {
            validatedObjectIds[parameterName] = null
        } else if (!ObjectId.isValid(parameter)) {
            call.respondText(
                text = "Invalid value for $parameterName parameter: $parameter is not a valid ObjectId",
                status = HttpStatusCode.BadRequest
            )
            return null
        } else {
            validatedObjectIds[parameterName] = ObjectId(parameter)
        }
    }
    return validatedObjectIds
}

suspend fun validateGroupBelonging(call: RoutingCall, groupRepository: GroupRepository, providedGroupId: ObjectId? = null): Boolean {
    val userSession = call.sessions.get<UserSession>()!!

    val groupId: ObjectId? = providedGroupId
        ?: validateRequiredObjectIds(call, "groupId")?.get("groupId")

    if (groupId == null || !groupRepository.isUserMember(groupId, ObjectId(userSession.id))) {
        call.smartRedirect("/")
        return false
    }
    return true
}