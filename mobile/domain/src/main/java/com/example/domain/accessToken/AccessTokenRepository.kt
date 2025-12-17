package com.example.domain.accessToken

import com.example.domain.auth.dataStore.DataStoreDataSource
import kotlinx.coroutines.flow.firstOrNull

interface AccessTokenRepository {
    suspend fun getAccessToken(): String?
    suspend fun setAccessToken(accessToken: String?)
    suspend fun getRefreshToken(): String?
    suspend fun setRefreshToken(refreshToken: String?)
    suspend fun hasAccessToken(): Boolean

}

class AccessTokenRepositoryImpl(
    private val dataStoreDataSource: DataStoreDataSource
) : AccessTokenRepository {
    override suspend fun getAccessToken() = dataStoreDataSource.accessToken.firstOrNull()

    override suspend fun setAccessToken(accessToken: String?) = dataStoreDataSource.setAccessToken(accessToken)

    override suspend fun getRefreshToken() = dataStoreDataSource.getRefreshToken()

    override suspend fun setRefreshToken(refreshToken: String?) = dataStoreDataSource.setRefreshToken(refreshToken)
    override suspend fun hasAccessToken() = getAccessToken() != null

}
