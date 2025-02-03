package com.physman.authentication.user

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

const val USERNAME_MIN = 5
const val USERNAME_MAX = 64
const val PASSWORD_MIN = 8
const val PASSWORD_MAX = 64


// TODO: Move these somewhere else pls
val usernameValidatorOnRegister = fun(username: String): String? {
    if(username.length < USERNAME_MIN) return "Username must be at least $USERNAME_MIN characters long."
    if(username.length > USERNAME_MAX) return "Username must be at most $USERNAME_MAX characters long."
    if(username.contains(' ')) return "Username must not contain spaces."
    // TODO: Username uniqueness and such
    return null
}

val passwordValidatorOnRegister = fun(password: String): String? {
    if(password.length < PASSWORD_MIN) return "Password too short."
    if(password.length > PASSWORD_MAX) return "Password too long."
    if(!Regex("^[a-zA-Z0-9 !@#\$%^&*()_+]*\$").matches(password)) return "Password contains forbidden characters."
    if(!Regex("^(?=.*[a-z])(?=.*[A-Z]).*\$").matches(password)) return "Password must contain a lowercase and an uppercase letter."
    if(!Regex("^(?=.*[0-9]).*\$").matches(password)) return "Password must contain a number."
    if(!Regex("^(?=.*[!@#\$%^&*()_+]).*\$").matches(password)) return "Password must contain a special character."
    return null
}

val usernameValidatorOnLogin = fun(username: String): String? {
    if (username.isBlank()) return "Username must not be blank"
    return null
}

val passwordValidatorOnLogin = fun(password: String): String? {
    if (password.isBlank()) return "Password must not be blank"
    return null
}


data class User(
    @BsonId
    val id: ObjectId = ObjectId(),
    val name: String,
    // val email: String, // hmmm
    val passwordHash: String,
    val groupIds: Set<ObjectId> = emptySet()
) {
    fun toUserSession() = UserSession(id.toHexString(), name)
}