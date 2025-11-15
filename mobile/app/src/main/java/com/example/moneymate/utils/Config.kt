package com.example.moneymate.utils

object Config {
    // This should come from your build config or dependency injection
    const val BASE_URL = "http://10.0.2.2:8000"

    // Helper function to build full URL for relative paths
    fun buildImageUrl(relativePath: String?): String? {
        return relativePath?.let { path ->
            if (path.startsWith("http")) {
                path // Already a full URL
            } else if (path.startsWith("/")) {
                "$BASE_URL$path" // Prepend base URL to relative path
            } else {
                "$BASE_URL/$path" // Prepend base URL and slash
            }
        }
    }
}