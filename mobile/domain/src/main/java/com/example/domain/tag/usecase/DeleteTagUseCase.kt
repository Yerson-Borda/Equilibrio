package com.example.domain.tag.usecase

import com.example.domain.tag.TagRepository

class DeleteTagUseCase(
    private val repository: TagRepository
) {
    suspend operator fun invoke(id: Int): Result<Unit> {
        return repository.deleteTag(id)
    }
}
