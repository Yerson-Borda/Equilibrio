package com.example.data.network.category.model

import com.example.domain.category.model.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val type: String,
    val color: String,
    val icon: String,
    val user_id: Int? = null
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            type = type,
            color = color,
            icon = icon,
            userId = user_id
        )
    }
}