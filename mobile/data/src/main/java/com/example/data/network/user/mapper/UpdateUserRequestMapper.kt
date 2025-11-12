// data/src/main/java/com/example/data/network/user/mapper/UpdateUserRequestMapper.kt
package com.example.data.network.user.mapper

import com.example.data.network.user.model.UpdateUserRequest
import com.example.domain.user.model.User

object UpdateUserRequestMapper {

    fun toRequest(user: User): UpdateUserRequest {
        return UpdateUserRequest(
            email = user.email,
            fullName = user.fullName.ifEmpty { null },
            phoneNumber = user.phoneNumber,
            dateOfBirth = user.birthDate
        )
    }
}