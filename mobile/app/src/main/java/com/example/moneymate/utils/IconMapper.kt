package com.example.moneymate.utils

import androidx.compose.ui.graphics.Color
import com.example.moneymate.R

object IconMapper {

    // Map backend icon names to drawable resources
    fun getIconDrawable(iconName: String?): Int {
        return when (iconName) {
            // Expense icons
            "utensils" -> R.drawable.ic_utensils
            "shopping-bag" -> R.drawable.ic_shopping_bag
            "home" -> R.drawable.ic_home
            "bus" -> R.drawable.ic_bus
            "car" -> R.drawable.ic_car
            "film" -> R.drawable.ic_film
            "phone" -> R.drawable.ic_phone
            "chart-line" -> R.drawable.ic_chart_line
            "ellipsis-h" -> R.drawable.ic_ellipsis_h

            // Income icons
            "arrow-in" -> R.drawable.ic_arrow_in
            "cards" -> R.drawable.ic_cards
            "arrow-out" -> R.drawable.ic_arrow_out
            "coins" -> R.drawable.ic_coins
            "money-hand" -> R.drawable.ic_money_hand
            "gift" -> R.drawable.ic_gift
            "check" -> R.drawable.ic_check
            "pencentage" -> R.drawable.ic_percentage
            // "home" is already mapped above (used for Rental Income)
            // "ellipsis-h" is already mapped above (used for Others)

            // Add more mappings as needed
            else -> R.drawable.ic_ellipsis_h // fallback icon
        }
    }

    // Parse color string to Compose Color
    fun parseColor(colorString: String?): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorString ?: "#666666"))
        } catch (e: Exception) {
            Color.Gray // fallback color
        }
    }

    // Get lighter background color for the circle (20% opacity of the main color)
    fun getBackgroundColor(colorString: String?): Color {
        return try {
            val originalColor = android.graphics.Color.parseColor(colorString ?: "#666666")
            Color(originalColor).copy(alpha = 0.2f)
        } catch (e: Exception) {
            Color.Gray.copy(alpha = 0.2f)
        }
    }
}