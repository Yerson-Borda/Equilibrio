// data/src/main/java/com/example/data/network/user/UserApi.kt
package com.example.data.network.user

import com.example.data.network.user.model.UpdateUserRequest
import com.example.data.network.user.model.UserDetailedResponse
import com.example.data.network.user.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    // For Home Screen - returns UserDetailedResponse directly
    @GET("api/v1/users/me/detailed")
    suspend fun getUserDetailed(): UserDetailedResponse

    // For Edit Profile Screen - returns Response<UserResponse> (changed from UserInfoResponse)
    @GET("api/v1/users/me")
    suspend fun getUser(): Response<UserResponse>

    @PUT("api/v1/users/me")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<UserResponse>

    @Multipart
    @POST("api/v1/users/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<UserResponse>

    @DELETE("api/v1/users/me/avatar")
    suspend fun deleteAvatar(): Response<UserResponse>
    @POST("api/v1/users/logout")
    suspend fun logout(): Response<Unit>
}