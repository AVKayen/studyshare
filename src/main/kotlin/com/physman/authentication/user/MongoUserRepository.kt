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
}