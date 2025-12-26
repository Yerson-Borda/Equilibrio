package com.example.domain.auth.dataStore

import kotlinx.coroutines.flow.Flow

interface DataStoreDataSource {
    val accessToken: Flow<String?>
suspend fun setAccessToken(accessToken: String?)
suspend fun getRefreshToken():String?
suspend fun setRefreshToken(refreshToken:String?)
suspend fun setUserId(userId: Int?)
suspend fun getUserId(): String?
}