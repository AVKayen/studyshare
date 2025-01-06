package com.physman.authentication.user

interface UserRepository {
    suspend fun createUser(user: User): User
    suspend fun getUserById(id: String): User?
    suspend fun getUserByName(name: String): User?
    suspend fun deleteUser(id: String): Unit
    suspend fun login(name: String, password: String): UserSession?
    suspend fun register(name: String, password: String): UserSession?
    suspend fun changePassword(name: String, previousPassword: String, newPassword: String): Unit
}