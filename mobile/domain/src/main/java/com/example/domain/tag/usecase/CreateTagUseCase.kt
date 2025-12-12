package com.example.domain.tag.usecase

import com.example.domain.tag.TagRepository
import com.example.domain.tag.model.CreateTag
import com.example.domain.tag.model.Tag

class CreateTagUseCase(
    private val repository: TagRepository
) {
    suspend operator fun invoke(name: String): Result<Tag> {
        val createTag = CreateTag(name = name)
        return repository.createTag(createTag)
    }
}