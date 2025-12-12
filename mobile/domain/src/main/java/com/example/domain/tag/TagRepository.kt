package com.example.domain.tag

import com.example.domain.tag.model.CreateTag
import com.example.domain.tag.model.Tag

interface TagRepository {
    suspend fun getTags(): Result<List<Tag>>
    suspend fun createTag(createTag: CreateTag): Result<Tag>
    suspend fun deleteTag(id: Int): Result<Unit>
}