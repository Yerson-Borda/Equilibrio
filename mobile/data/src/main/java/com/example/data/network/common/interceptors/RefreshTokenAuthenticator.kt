package com.example.data.network.common.interceptors

import android.util.Log
import com.example.data.network.common.model.TokenRequest
import com.example.data.network.common.model.TokenResponse
import com.example.domain.accessToken.AccessTokenRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun Call.await(recordStack: Boolean = true): Response {
    val callStack = if (recordStack) {
        IOException().apply {
            stackTrace = stackTrace.copyOfRange(1, stackTrace.size)
        }
    } else null

    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) return
                callStack?.initCause(e)
                continuation.resumeWithException(callStack ?: e)
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (_: Throwable) {}
        }
    }
}

class RefreshTokenAuthenticator(
    private val accessTokenRepository: AccessTokenRepository,
    private val apiUrl: String,
    private val serializer: Json
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val token = runBlocking {
            accessTokenRepository.getRefreshToken().also {
                Log.d(TAG, "Current refresh token: $it")
            }
        }

        if (token.isNullOrBlank()) {
            Log.w(TAG, "No refresh token available.")
            return null
        }

        return runBlocking {
            Log.d(TAG, "Attempting to refresh token...")
            val newToken = getNewToken(TokenRequest(token))

            if (newToken == null) {
                Log.e(TAG, "Token refresh failed, clearing saved tokens.")
                accessTokenRepository.setAccessToken(null)
                accessTokenRepository.setRefreshToken(null)
                return@runBlocking null
            }

            Log.d(TAG, "Token refresh successful. New accessToken: ${newToken.accessToken}")

            accessTokenRepository.setAccessToken(newToken.accessToken)
            accessTokenRepository.setRefreshToken(newToken.refreshToken)

            response.request.newBuilder()
                .header(HeadersInterceptor.HEADER_AUTH, "${HeadersInterceptor.HEADER_BEARER} ${newToken.accessToken}")
                .build()
        }
    }

    private suspend fun getNewToken(refreshToken: TokenRequest): TokenResponse? {
        Log.d(TAG, "Sending refresh request to: $apiUrl$REFRESH_PATH")
        val okHttpClient = createOkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val requestBody = serializer.encodeToString(TokenRequest.serializer(), refreshToken)
        Log.d(TAG, "Refresh request body: $requestBody")

        val request = Request.Builder()
            .url("$apiUrl$REFRESH_PATH")
            .post(requestBody.toRequestBody(mediaType))
            .build()

        val response = okHttpClient.newCall(request).await()

        Log.d(TAG, "Refresh response code: ${response.code}")

        if (!response.isSuccessful) {
            Log.e(TAG, "Unsuccessful refresh response: ${response.code} - ${response.message}")
            return null
        }

        return try {
            val body = response.body?.string()
            Log.d(TAG, "Refresh response body: $body")
            if (body.isNullOrBlank()) {
                Log.w(TAG, "Refresh response body is empty.")
                null
            } else {
                serializer.decodeFromString(TokenResponse.serializer(), body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding token response", e)
            null
        }
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            val logLevel = HttpLoggingInterceptor.Level.BODY
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = logLevel
            }
            addInterceptor(httpLoggingInterceptor)
        }.build()
    }

    private companion object {
        const val TAG = "RefreshTokenAuth"
        const val REFRESH_PATH = "v1/auth/refresh"
    }
}