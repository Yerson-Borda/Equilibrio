package com.example.data.network.categoryLimit.model
import com.example.domain.categoryLimit.model.CategoryLimit
import com.example.domain.categoryLimit.model.CategoryLimitOverview
import com.example.domain.categoryLimit.model.CategoryLimitUpdate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryLimitResponseDto(
    @SerialName("id")
    val id: Int,
    @SerialName("category_id")
    val categoryId: Int,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("monthly_limit")
    val monthlyLimit: String
)

@Serializable
data class CategoryLimitOverviewItemDto(
    @SerialName("category_id")
    val categoryId: Int,
    @SerialName("category_name")
    val categoryName: String,
    @SerialName("category_color")
    val categoryColor: String? = null,
    @SerialName("category_icon")
    val categoryIcon: String? = null,
    @SerialName("monthly_limit")
    val monthlyLimit: String,
    @SerialName("monthly_spent")
    val monthlySpent: String
)

@Serializable
data class CategoryLimitUpdateRequestDto(
    @SerialName("monthly_limit")
    val monthlyLimit: String
)

// Extension functions for mapping
fun CategoryLimitResponseDto.toDomain(): CategoryLimit {
    return CategoryLimit(
        id = id,
        categoryId = categoryId,
        userId = userId,
        monthlyLimit = monthlyLimit
    )
}

fun CategoryLimitOverviewItemDto.toDomain(): CategoryLimitOverview {
    return CategoryLimitOverview(
        categoryId = categoryId,
        categoryName = categoryName,
        categoryColor = categoryColor,
        categoryIcon = categoryIcon,
        monthlyLimit = monthlyLimit,
        monthlySpent = monthlySpent
    )
}

fun CategoryLimitUpdate.toDto(): CategoryLimitUpdateRequestDto {
    return CategoryLimitUpdateRequestDto(monthlyLimit = monthlyLimit)
}