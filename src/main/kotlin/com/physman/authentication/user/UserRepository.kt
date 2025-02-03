package com.physman.authentication.user

import org.bson.types.ObjectId

interface UserRepository {
    suspend fun createUser(user: User): User
    suspend fun getUserById(id: String): User?
    suspend fun getUserByName(name: String): User?
    suspend fun getUsersByIds(ids: List<ObjectId>): List<User>
    suspend fun deleteUser(id: String)
    suspend fun addGroupToUser(userId: ObjectId, groupId: ObjectId)
    suspend fun removeGroupFromUser(userId: ObjectId, groupId: ObjectId)
    suspend fun login(name: String, password: String): UserSession?
    suspend fun register(name: String, password: String): UserSession
    suspend fun changePassword(name: String, previousPassword: String, newPassword: String)
}