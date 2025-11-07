package com.example.domain.home

import com.example.domain.home.model.HomeData

interface HomeRepository {
    suspend fun getHomeData(): HomeData
}