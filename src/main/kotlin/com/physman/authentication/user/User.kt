package com.physman.authentication.user

import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import org.bson.types.ObjectId

const val USERNAME_MIN = 5
const val USERNAME_MAX = 64
const val PASSWORD_MIN = 8
const val PASSWORD_MAX = 64


// TODO: Move these somewhere else pls
val usernameValidatorOnRegister = fun(username: String): String? {
    if(username.length < USERNAME_MIN) return "Username must be at least $USERNAME_MIN characters long"
    if(username.length > USERNAME_MAX) return "Username must be at most $USERNAME_MAX characters long"
    if(username.contains(' ')) return "Username must not contain spaces"
    // TODO: Username uniqueness and such
    return null
}

val passwordValidatorOnRegister = fun(password: String): String? {
    if(password.length < PASSWORD_MIN) return "Password must be at least $PASSWORD_MIN characters long"
    if(password.length > PASSWORD_MAX) return "Password must be at most $PASSWORD_MAX characters long"
    // TODO: Stronger password requirements
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
    @BsonRepresentation(BsonType.OBJECT_ID)

    val id: String = ObjectId().toHexString(),
    val name: String,
    // val email: String, // hmmm
    val passwordHash: String,
) {
    fun toUserSession() = UserSession(id, name)
}