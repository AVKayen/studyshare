package com.physman.authentication.user

import org.bson.BsonType
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonRepresentation
import org.bson.types.ObjectId

data class User(
    @BsonId
    @BsonRepresentation(BsonType.OBJECT_ID)

    val id: String = ObjectId().toHexString(),
    val name: String,
    // val email: String, // hmmm
    val passwordHash: String,
) {
    fun toUserSession() = UserSession(name)
}