
package com.example.data.network.common.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
)