package com.example.data.network.tag.model

import com.example.domain.tag.model.Tag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("user_id") val userId: Int
) {
    fun toEntity(): Tag {
        return Tag(
            id = id,
            name = name,
            userId = userId
        )
    }
}