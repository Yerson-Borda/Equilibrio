package com.example.domain.common

import java.io.IOException

data class MoneyMateError(override val message: String) : IOException(message)