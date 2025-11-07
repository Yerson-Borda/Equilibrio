package com.example.data.network.home

import com.example.data.network.home.model.HomeResponse
import retrofit2.http.GET

interface HomeApi {
    @GET("api/v1/home")
    suspend fun getHomeData(): HomeResponse
}