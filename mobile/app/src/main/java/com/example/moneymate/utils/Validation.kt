package com.example.moneymate.utils

object Validation {
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
        return email.matches(emailRegex)
    }

    fun isValidPassword(password: String): ValidationResult {
        return ValidationResult(
            isValid = password.length >= 8 &&
                    password.any { it.isUpperCase() } &&
                    password.any { it.isDigit() } &&
                    password.any { !it.isLetterOrDigit() },
            errorMessage = if (password.isNotEmpty()) {
                val missingRequirements = mutableListOf<String>()
                if (password.length < 8) missingRequirements.add("at least 8 characters")
                if (!password.any { it.isUpperCase() }) missingRequirements.add("uppercase letter")
                if (!password.any { it.isDigit() }) missingRequirements.add("number")
                if (!password.any { !it.isLetterOrDigit() }) missingRequirements.add("symbol")

                if (missingRequirements.isNotEmpty()) {
                    "Use ${missingRequirements.joinToString(", ")}"
                } else {
                    null
                }
            } else {
                null
            }
        )
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
}