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
    @GET("api/v1/categories/")
    suspend fun getCategories(): List<CategoryResponse>

    @GET("api/v1/categories/income")
    suspend fun getIncomeCategories(): List<CategoryResponse>

    @GET("api/v1/categories/expense")
    suspend fun getExpenseCategories(): List<CategoryResponse>

    @POST("api/v1/categories/")
    suspend fun createCategory(@Body request: CategoryCreateRequest): CategoryResponse

    @DELETE("api/v1/categories/{category_id}")
    suspend fun deleteCategory(@Path("category_id") categoryId: Int): Response<Unit>
}