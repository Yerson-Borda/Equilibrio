package com.example.data.network.home

import com.example.data.network.home.model.UserDetailedResponse
import retrofit2.http.GET

interface UserApi {
    @GET("api/v1/users/me/detailed")
    suspend fun getUserDetailed(): UserDetailedResponse
}