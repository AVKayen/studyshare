package com.physman.authentication.user

interface UserRepository {
    suspend fun createUser(user: User): User
    suspend fun getUserById(id: String): User?
    suspend fun getUserByName(name: String): User?
    suspend fun deleteUser(id: String): Unit
}