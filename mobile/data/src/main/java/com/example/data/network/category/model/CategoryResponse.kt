package com.example.data.network.category.model

import com.example.domain.category.model.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("color") val color: String? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("user_id") val userId: Int? = null
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            type = type,
            color = color,
            icon = icon,
            userId = userId
        )
    }
}
