package com.example.domain.tag.usecase

import com.example.domain.tag.TagRepository
import com.example.domain.tag.model.Tag

class GetTagsUseCase(
    private val repository: TagRepository
) {
    suspend operator fun invoke(): Result<List<Tag>> {
        return repository.getTags()
    }
}