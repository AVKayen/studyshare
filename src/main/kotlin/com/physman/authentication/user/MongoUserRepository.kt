package com.physman.authentication.user

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.client.model.Filters.*
import kotlinx.coroutines.flow.firstOrNull
import org.mindrot.jbcrypt.BCrypt

class MongoUserRepository(database: MongoDatabase) : UserRepository {
    private val users = database.getCollection<User>("users")

    override suspend fun createUser(user: User): User {
        users.insertOne(user)
        return user
    }

    override suspend fun getUserById(id: String): User? {
        return users.find(eq(User::id.name, id)).firstOrNull()
    }

    override suspend fun getUserByName(name: String): User? {
        return users.find(eq(User::name.name, name)).firstOrNull()
    }

    override suspend fun deleteUser(id: String) {
        users.findOneAndDelete(eq(User::id.name, id))
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