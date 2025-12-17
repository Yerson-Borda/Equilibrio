// data/src/main/java/com/example/data/network/user/UserRepositoryImpl.kt
package com.example.data.network.user

import com.example.data.network.user.mapper.UpdateUserRequestMapper
import com.example.data.network.user.mapper.UserDetailedResponseMapper
import com.example.data.network.user.mapper.UserResponseMapper
import com.example.domain.user.UserRepository
import com.example.domain.user.model.User
import com.example.domain.user.model.UserDetailedData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepositoryImpl(
    private val userApi: UserApi
) : UserRepository {

    // For Home Screen - returns UserDetailedData directly
    override suspend fun getUserDetailed(): UserDetailedData {
        val response = userApi.getUserDetailed()
        return UserDetailedResponseMapper.toDomain(response)
    }

    // For Edit Profile Screen - returns Result<User>
    override suspend fun getUser(): Result<User> {
        return runCatching {
            val response = userApi.getUser()
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    UserResponseMapper.toDomain(userResponse)
                } else {
                    throw Exception("User data is null")
                }
            } else {
                throw Exception("Failed to get user: ${getErrorMessage(response.code(), response.message())}")
            }
        }
    }

    override suspend fun updateUser(user: User): Result<User> {
        return runCatching {
            val request = UpdateUserRequestMapper.toRequest(user)
            val response = userApi.updateUser(request)
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    UserResponseMapper.toDomain(userResponse)
                } else {
                    throw Exception("Updated user data is null")
                }
            } else {
                throw Exception("Failed to update user: ${getErrorMessage(response.code(), response.message())}")
            }
        }
    }

    override suspend fun uploadAvatar(avatarUri: String): Result<User> {
        return runCatching {
            println("DEBUG: Repository - Uploading avatar from URI: $avatarUri")

            val file = File(avatarUri)
            if (!file.exists()) {
                throw Exception("Avatar file does not exist at path: $avatarUri")
            }

            // Get the file name and extension
            val fileName = file.name
            val fileExtension = fileName.substringAfterLast(".", "")
            val mimeType = when (fileExtension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> "image/*"
            }

            println("DEBUG: Repository - File: $fileName, MIME type: $mimeType")

            val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())

            // Use "file" as the parameter name (not "avatar") to match the API
            val part = MultipartBody.Part.createFormData(
                "file", // Changed from "avatar" to "file"
                fileName,
                requestBody
            )

            val response = userApi.uploadAvatar(part)
            println("DEBUG: Repository - Upload response code: ${response.code()}")

            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    println("DEBUG: Repository - Avatar upload successful")
                    UserResponseMapper.toDomain(userResponse)
                } else {
                    throw Exception("Avatar upload response is null")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG: Repository - Upload error body: $errorBody")
                throw Exception("Failed to upload avatar: ${getErrorMessage(response.code(), response.message())}")
            }
        }
    }

    override suspend fun deleteAvatar(): Result<User> {
        return runCatching {
            val response = userApi.deleteAvatar()
            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null) {
                    UserResponseMapper.toDomain(userResponse)
                } else {
                    throw Exception("Avatar delete response is null")
                }
            } else {
                throw Exception("Failed to delete avatar: ${getErrorMessage(response.code(), response.message())}")
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return runCatching {
            val response = userApi.logout()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                throw Exception("Failed to logout: ${getErrorMessage(response.code(), response.message())}")
            }
        }
    }


    private fun getErrorMessage(code: Int, defaultMessage: String): String {
        return when (code) {
            400 -> "Bad request - please check your input data"
            401 -> "Unauthorized - please login again"
            403 -> "Forbidden - access denied"
            404 -> "Resource not found"
            409 -> "Conflict - data already exists"
            422 -> "Validation error - check your input fields"
            500 -> "Server error - please try again later"
            else -> "Error $code: $defaultMessage"
        }
    }
}