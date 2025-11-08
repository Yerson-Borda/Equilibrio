package com.example.domain.home

import com.example.domain.home.model.UserDetailedData


interface UserRepository {
    suspend fun getUserDetailed(): UserDetailedData
}