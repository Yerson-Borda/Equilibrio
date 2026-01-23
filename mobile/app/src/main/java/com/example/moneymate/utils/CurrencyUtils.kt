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
    
    /**
     * Convert amount from source currency to target currency
     * Note: This uses approximate exchange rates. Actual conversion will be done by backend.
     */
    fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Double {
        if (fromCurrency == toCurrency) return amount
        
        // Convert to USD first (as base currency)
        val amountInUSD = amount / getExchangeRateToUSD(fromCurrency)
        
        // Convert from USD to target currency
        return amountInUSD * getExchangeRateToUSD(toCurrency)
    }
    
    /**
     * Get approximate exchange rate to USD
     * These are approximate rates and should be updated periodically
     * Actual conversion will be done by backend with real-time rates
     */
    private fun getExchangeRateToUSD(currencyCode: String): Double {
        return when (currencyCode.uppercase()) {
            "USD" -> 1.0
            "EUR" -> 0.92      // 1 EUR ≈ 1.09 USD
            "GBP" -> 0.79      // 1 GBP ≈ 1.27 USD
            "JPY" -> 149.0     // 1 USD ≈ 149 JPY
            "CAD" -> 1.36      // 1 CAD ≈ 0.74 USD
            "AUD" -> 1.52      // 1 AUD ≈ 0.66 USD
            "CHF" -> 0.88      // 1 CHF ≈ 1.14 USD
            "CNY" -> 7.24      // 1 USD ≈ 7.24 CNY
            "INR" -> 83.0      // 1 USD ≈ 83 INR
            "RUB" -> 92.0      // 1 USD ≈ 92 RUB
            "BRL" -> 5.0       // 1 USD ≈ 5 BRL
            "MXN" -> 17.0      // 1 USD ≈ 17 MXN
            "KRW" -> 1320.0    // 1 USD ≈ 1320 KRW
            else -> 1.0        // Fallback
        }
    }
}