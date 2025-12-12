package com.example.data.network.category

import com.example.data.network.category.model.CategoryCreateRequest
import com.example.data.network.category.model.CategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CategoryApi {
    @GET("api/categories/")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @POST("api/categories/")
    suspend fun createCategory(@Body request: CategoryCreateRequest): Response<CategoryResponse>

    @GET("api/categories/income")
    suspend fun getIncomeCategories(): Response<List<CategoryResponse>>

    @GET("api/categories/expense")
    suspend fun getExpenseCategories(): Response<List<CategoryResponse>>

    @DELETE("api/categories/{category_id}")
    suspend fun deleteCategory(@Path("category_id") categoryId: Int): Response<Unit>
}