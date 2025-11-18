package com.example.data.network.category.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryCreateRequest(
    val name: String,
    val type: String,
    val color: String,
    val icon: String
)