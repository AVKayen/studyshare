package com.studyshare.authentication.user

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val id: String,
    val name: String,
)