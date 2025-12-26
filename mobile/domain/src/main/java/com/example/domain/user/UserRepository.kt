package com.example.domain.user

import com.example.domain.user.model.User
import com.example.domain.user.model.UserDetailedData


interface UserRepository {
    suspend fun getUserDetailed(): UserDetailedData
    suspend fun getUser(): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun uploadAvatar(avatarUri: String): Result<User>
    suspend fun deleteAvatar(): Result<User>
    suspend fun logout(): Result<Unit>
}