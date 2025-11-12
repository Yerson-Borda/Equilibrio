package com.example.data.network.user

import com.example.data.network.user.model.StatsResponse
import com.example.data.network.user.model.UpdateUserRequest
import com.example.data.network.user.model.UserDetailedResponse
import com.example.data.network.user.model.UserResponse
import com.example.domain.user.UserRepository
import com.example.domain.user.model.StatsData
import com.example.domain.user.model.User
import com.example.domain.user.model.UserData
import com.example.domain.user.model.UserDetailedData
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepositoryImpl(
    private val userApi: UserApi
) : UserRepository {

    override suspend fun getUserDetailed(): UserDetailedData {
        val response = userApi.getUserDetailed()
        return response.toDomain()
    }

    override suspend fun getUser(): Result<User> {
        return try {
            val response = userApi.getUser()
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    Result.Success(userResponse.toDomain())
                } else {
                    Result.Failure(Exception("User data is null"))
                }
            } else {
                Result.Failure(Exception("Failed to get user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val request = UpdateUserRequest(
                fullName = user.fullName,
                email = user.email,
                phoneNumber = user.phoneNumber,
                birthDate = user.birthDate
            )
            val response = userApi.updateUser(request)
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    Result.Success(userResponse.toDomain())
                } else {
                    Result.Failure(Exception("Updated user data is null"))
                }
            } else {
                Result.Failure(Exception("Failed to update user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun uploadAvatar(avatarUri: String): Result<User> {
        return try {
            val file = File(avatarUri)
            val requestBody = file.asRequestBody()
            val part = MultipartBody.Part.createFormData(
                "avatar",
                file.name,
                requestBody
            )

            val response = userApi.uploadAvatar(part)
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    Result.Success(userResponse.toDomain())
                } else {
                    Result.Failure(Exception("Avatar upload response is null"))
                }
            } else {
                Result.Failure(Exception("Failed to upload avatar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun deleteAvatar(): Result<User> {
        return try {
            val response = userApi.deleteAvatar()
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    Result.Success(userResponse.toDomain())
                } else {
                    Result.Failure(Exception("Avatar delete response is null"))
                }
            } else {
                Result.Failure(Exception("Failed to delete avatar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    // Extension functions to convert from network to domain models
    private fun UserDetailedResponse.toDomain(): UserDetailedData {
        return UserDetailedData(
            user = user.toDomain(),
            stats = stats.toDomain()
        )
    }

    private fun UserResponse.toDomain(): UserData {
        return UserData(
            id = id.toString(),
            email = email,
            fullName = fullName,
            phoneNumber = phoneNumber,
            dateOfBirth = dateOfBirth,
            avatarUrl = avatarUrl,
            defaultCurrency = defaultCurrency,
            createdAt = createdAt
        )
    }

    private fun StatsResponse.toDomain(): StatsData {
        return StatsData(
            walletCount = walletCount,
            totalTransactions = totalTransactions,
            expenseCount = expenseCount,
            incomeCount = incomeCount
        )
    }
}
