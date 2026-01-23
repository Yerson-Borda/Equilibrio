package com.example.data.network.goal.model

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.domain.goal.model.Goal
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class GoalResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val image: String?,
    val deadline: String?, // ISO format date string
    val goal_amount: String,
    val amount_saved: String,
    val wallet_id: Int,
    val currency: String
)

@Serializable
data class GoalUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val deadline: String? = null,
    val goal_amount: String? = null
)

// Note: For create, we use multipart form data, so no separate request model needed

@RequiresApi(Build.VERSION_CODES.O)
fun GoalResponse.toDomain(): Goal {
    return Goal(
        id = id,
        title = title,
        description = description,
        image = image,
        deadline = deadline?.let { LocalDate.parse(it) },
        goalAmount = goal_amount.toDoubleOrNull() ?: 0.0,
        amountSaved = amount_saved.toDoubleOrNull() ?: 0.0,
        walletId = wallet_id,
        currency = currency
    )
}