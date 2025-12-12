package com.example.data.network.tag

import com.example.data.network.tag.model.CreateTagRequest
import com.example.data.network.tag.model.TagDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TagApi{
    @GET("/api/tags/")
    suspend fun getTags(): List<TagDto>

    @POST("/api/tags/")
    suspend fun createTag(@Body request: CreateTagRequest): TagDto

    @DELETE("/api/tags/{id}")
    suspend fun deleteTag(@Path("id") id: Int)
}