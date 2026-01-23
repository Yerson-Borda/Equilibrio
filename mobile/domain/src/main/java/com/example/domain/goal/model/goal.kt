package com.example.domain.goal.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

data class Goal(
    val id: Int,
    val title: String,
    val description: String?,
    val image: String?,
    val deadline: LocalDate?,
    val goalAmount: Double,
    val amountSaved: Double,
    val walletId: Int,
    val currency: String
) {
    val progress: Float
        get() = if (goalAmount > 0) (amountSaved / goalAmount).toFloat().coerceIn(0f, 1f) else 0f

    val remainingAmount: Double
        get() = (goalAmount - amountSaved).coerceAtLeast(0.0)

    val isAchieved: Boolean
        get() = amountSaved >= goalAmount

    val daysRemaining: Long?
        @RequiresApi(Build.VERSION_CODES.O)
        get() = deadline?.let {
            val today = LocalDate.now()
            if (today.isBefore(it)) {
                today.until(it).days.toLong()
            } else {
                0L
            }
        }
}