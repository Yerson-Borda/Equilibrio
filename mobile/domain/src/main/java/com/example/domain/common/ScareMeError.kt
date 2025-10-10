package com.example.domain.common

import java.io.IOException

data class ScareMeError(override val message: String) : IOException(message)
