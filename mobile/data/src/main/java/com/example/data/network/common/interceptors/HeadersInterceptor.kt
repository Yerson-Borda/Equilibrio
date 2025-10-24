package com.example.data.network.common.interceptors

import android.util.Log
import com.example.domain.accessToken.AccessTokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

@Suppress("TooGenericExceptionCaught", "SwallowedException")
class HeadersInterceptor(
    private val accessTokenRepository: AccessTokenRepository,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { accessTokenRepository.getAccessToken() }
        val response: Response?
        val baseRequest = chain.request()
        val request: Request = baseRequest.newBuilder().apply {
            // add common headers over all requests
            addHeader("Accept", "*/*")

            // add headers depending on method type
            when (baseRequest.method) {
                GET_METHOD -> {
                    addHeader("Accept", "application/json")
                }

                POST_METHOD -> {
                    addHeader("Content-Type", "application/json")
                }
            }

            // add token if exists
            if (!token.isNullOrBlank()) {
                addHeader(HEADER_AUTH, "$HEADER_BEARER $token")
            }
        }.build()
        Log.d("HeadersInterceptor", "Final headers: ${request.headers}")
        return try {
            response = chain.proceed(request)
            response
        } catch (e: Exception) {
            chain.proceed(request)
        }
    }

    companion object {
        const val HEADER_AUTH: String = "Authorization"
        const val HEADER_BEARER: String = "Bearer"
        const val GET_METHOD: String = "GET"
        const val POST_METHOD: String = "POST"
    }
}
