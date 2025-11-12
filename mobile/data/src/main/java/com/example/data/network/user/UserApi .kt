package com.example.data.network.user

import com.example.data.network.user.model.UpdateUserRequest
import com.example.data.network.user.model.UserDetailedResponse
import com.example.data.network.user.model.UserInfoResponse
import com.example.data.network.user.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserApi {
    @GET("api/v1/users/me/detailed")
    suspend fun getUserDetailed(): UserDetailedResponse
    @GET("/api/v1/users/me")
    suspend fun getUser(): Response<UserInfoResponse>

    @PUT("/api/v1/users/me")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<UserResponse>

    @Multipart
    @POST("/api/v1/users/me/avatar")
    suspend fun uploadAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<UserResponse>

    @DELETE("/api/v1/users/me/avatar")
    suspend fun deleteAvatar(): Response<UserResponse>
}