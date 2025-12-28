package com.example.data.network.categoryLimit
import com.example.data.network.categoryLimit.model.CategoryLimitOverviewItemDto
import com.example.data.network.categoryLimit.model.CategoryLimitResponseDto
import com.example.data.network.categoryLimit.model.CategoryLimitUpdateRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoryLimitApi {
    @GET("/api/limits/")
    suspend fun getCategoryLimits(): Response<List<CategoryLimitOverviewItemDto>>

    @PUT("/api/limits/{category_id}")
    suspend fun updateCategoryLimit(
        @Path("category_id") categoryId: Int,
        @Body limitUpdate: CategoryLimitUpdateRequestDto
    ): Response<CategoryLimitResponseDto>

    @DELETE("/api/limits/{category_id}")
    suspend fun deleteCategoryLimit(
        @Path("category_id") categoryId: Int
    ): Response<Unit>
}