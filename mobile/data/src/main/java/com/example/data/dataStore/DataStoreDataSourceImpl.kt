package com.example.data.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.auth.dataStore.DataStoreDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_name")

class DataStoreDataSourceImpl(
    context: Context
) : DataStoreDataSource {

    private val dataStore = context.dataStore

    // Access Token Implementation
    override val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]?.takeIf { it.isNotEmpty() }
    }

    override suspend fun setAccessToken(accessToken: String?) {
        dataStore.edit { preferences ->
            if (accessToken != null) {
                preferences[ACCESS_TOKEN] = accessToken
            } else {
                preferences.remove(ACCESS_TOKEN)
            }
        }
    }

    // Refresh Token Implementation
    override suspend fun getRefreshToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN]?.takeIf { it.isNotEmpty() }
        }.first()
    }

    override suspend fun setRefreshToken(refreshToken: String?) {
        dataStore.edit { preferences ->
            if (refreshToken != null) {
                preferences[REFRESH_TOKEN] = refreshToken
            } else {
                preferences.remove(REFRESH_TOKEN)
            }
        }
    }

    // User ID Implementation
    override suspend fun setUserId(userId: String?) {
        dataStore.edit { preferences ->
            if (userId != null) {
                preferences[USER_ID] = userId
            } else {
                preferences.remove(USER_ID)
            }
        }
    }

    override suspend fun getUserId(): String? {
        return dataStore.data.map { preferences ->
            preferences[USER_ID]?.takeIf { it.isNotEmpty() }
        }.first()
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
    }
}