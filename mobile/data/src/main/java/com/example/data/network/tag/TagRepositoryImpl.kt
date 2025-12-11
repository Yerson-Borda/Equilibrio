package com.example.data.network.tag

import com.example.data.network.tag.model.CreateTagRequest
import com.example.domain.tag.TagRepository
import com.example.domain.tag.model.CreateTag
import com.example.domain.tag.model.Tag

class TagRepositoryImpl (
    private val apiService: TagApi
) : TagRepository {

    override suspend fun getTags(): Result<List<Tag>> {
        return try {
            val response = apiService.getTags()
            Result.success(response.map { it.toEntity() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createTag(createTag: CreateTag): Result<Tag> {
        return try {
            // Convert domain model to data model
            val request = CreateTagRequest.fromDomain(createTag)
            val response = apiService.createTag(request)
            Result.success(response.toEntity())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTag(id: Int): Result<Unit> {
        return try {
            apiService.deleteTag(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}