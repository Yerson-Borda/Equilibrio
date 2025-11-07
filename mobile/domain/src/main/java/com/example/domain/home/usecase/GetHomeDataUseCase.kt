package com.example.domain.home.usecase

import com.example.domain.home.HomeRepository
import com.example.domain.home.model.HomeData

class GetHomeDataUseCase(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): HomeData {
        return homeRepository.getHomeData()
    }
}