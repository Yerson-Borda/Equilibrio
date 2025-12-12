package com.example.data.network.tag.model

import com.example.domain.tag.model.CreateTag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTagRequest(
    @SerialName("name") val name: String
) {
    companion object {
        fun fromDomain(createTag: CreateTag): CreateTagRequest {
            return CreateTagRequest(
                name = createTag.name
            )
        }
    }
}