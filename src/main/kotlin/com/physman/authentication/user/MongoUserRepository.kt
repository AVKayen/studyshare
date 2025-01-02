package com.physman.authentication.user

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.client.model.Filters.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class MongoUserRepository(private val database: MongoDatabase) : UserRepository {
    private val users = database.getCollection<User>("users")

    override suspend fun createUser(user: User): User {
        users.insertOne(user)
        return user
    }

    override suspend fun getUserById(id: String): User? {
        return users.find(Filters.eq(User::id.name, id)).firstOrNull()
    }

    override suspend fun getUserByName(name: String): User? {
        return users.find(Filters.eq(User::name.name, name)).firstOrNull()
    }

    override suspend fun deleteUser(id: String) {
        users.findOneAndDelete(Filters.eq(User::id.name, id))
    }

    override suspend fun login(name: String, password: String): UserSession? {
        val user: User = users.find(Filters.eq(User::name.name, name)).firstOrNull() ?: return null
        if (user.passwordHash == password) {
            // TODO: Password hashing!!!!!!!!! Important very much
            return user.toUserSession()
        }
        return null
    }

    override suspend fun register(name: String, password: String): UserSession {
        // TODO: Password hashing!!!!!!!!! Important very much
        val user = User(name = name, passwordHash = password)
        users.insertOne(user)
        return user.toUserSession()
    }

    override suspend fun changePassword(name: String, previousPassword: String, newPassword: String) {
        TODO("Not yet implemented")
    }
}