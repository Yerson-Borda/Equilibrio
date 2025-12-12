package com.example.moneymate.utils

object CurrencyUtils {
    fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CAD" -> "C$"
            "AUD" -> "A$"
            "CHF" -> "CHF"
            "CNY" -> "¥"
            "INR" -> "₹"
            "RUB" -> "₽"
            "BRL" -> "R$"
            "MXN" -> "$"
            "KRW" -> "₩"
            // Add more currencies as needed
            else -> currencyCode // Fallback to the code itself
        }
    }
}