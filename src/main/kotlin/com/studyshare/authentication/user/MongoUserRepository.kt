package com.studyshare.authentication.user

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.addToSet
import com.mongodb.client.model.Updates.pull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt

class MongoUserRepository(database: MongoDatabase) : UserRepository {
    private val users = database.getCollection<User>("users")

    override suspend fun createUser(user: User): User {
        users.insertOne(user)
        return user
    }

    override suspend fun getUserById(id: String): User? {
        return users.find(eq("_id", ObjectId(id))).firstOrNull()
    }

    override suspend fun getUsersByIds(ids: List<ObjectId>): List<User> {
        return users.find(`in`("_id", ids)).toList()
    }

    override suspend fun getUserByName(name: String): User? {
        return users.find(eq(User::name.name, name)).firstOrNull()
    }

    override suspend fun deleteUser(id: String) {
        users.findOneAndDelete(eq("_id", ObjectId(id)))
    }

    override suspend fun addGroupToUser(userId: ObjectId, groupId: ObjectId) {
        users.updateOne(eq("_id", userId), addToSet(User::groupIds.name, groupId))
    }

    override suspend fun removeGroupFromUser(userId: ObjectId, groupId: ObjectId) {
        users.updateOne(eq("_id", userId), pull(User::groupIds.name, groupId))
    }

    override suspend fun login(name: String, password: String): UserSession? {
        val user: User = users.find(eq(User::name.name, name)).firstOrNull() ?: return null
        if (BCrypt.checkpw(password, user.passwordHash)) {
            return user.toUserSession()
        }
        return null
    }

    override suspend fun register(name: String, password: String): UserSession {
        val existingUserData = users.find(eq(User::name.name, name)).firstOrNull()
        if (existingUserData != null) {
            throw IllegalArgumentException("Username already taken")
        }
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = User(name = name, passwordHash = passwordHash)
        val result = users.insertOne(user)
        if (!result.wasAcknowledged()) {
            throw Exception("Failed to insert user")
        }
        return user.toUserSession()
    }

    override suspend fun changePassword(name: String, previousPassword: String, newPassword: String) {
        TODO("Not yet implemented")
    }
}