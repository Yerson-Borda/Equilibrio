package com.example.data.network.common.interceptors

import android.util.Log
import com.example.data.network.common.model.ErrorResponse
import com.example.domain.accessToken.AccessTokenRepository
import com.example.domain.common.ScareMeError
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection

@Suppress("MagicNumber", "TooGenericExceptionCaught", "SwallowedException")
class StatusCodeInterceptor(
    private val accessTokenRepository: AccessTokenRepository,
    private val deserializer: Json
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return handleResponse(chain.proceed(request))
    }

    private fun handleResponse(response: Response): Response {
        Log.d("StatusCodeInterceptor", "Response Code: ${response.code}")
        Log.d("StatusCodeInterceptor", "Response Body: ${response.peekBody(Long.MAX_VALUE).string()}")

        when (response.code) {
            in HttpURLConnection.HTTP_OK..HttpURLConnection.HTTP_MULT_CHOICE -> null
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                runBlocking {
                    accessTokenRepository.setAccessToken(null)
                    accessTokenRepository.setRefreshToken(null)
                }
                IOException(response.message)
            }

            else -> {
                runCatching {
                    val errorResponse = deserializer.decodeFromString(
                        ErrorResponse.serializer(),
                        response.body?.string().orEmpty()
                    )
                    ScareMeError(errorResponse.message)
                }.getOrDefault(IOException(response.message))
            }
        }?.let { ioException -> throw ioException }

        return response
    }
}
