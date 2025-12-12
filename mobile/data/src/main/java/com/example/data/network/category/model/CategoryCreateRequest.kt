package com.example.data.network.category.model

import com.example.domain.category.model.CategoryCreateRequest as DomainCategoryCreateRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryCreateRequest(
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("color") val color: String? = null,
    @SerialName("icon") val icon: String? = null
) {
    companion object {
        fun fromDomain(domain: DomainCategoryCreateRequest): CategoryCreateRequest {
            return CategoryCreateRequest(
                name = domain.name,
                type = domain.type,
                color = domain.color,
                icon = domain.icon
            )
        }
    }
}