// data/src/main/java/com/example/data/network/user/mapper/UserDetailedResponseMapper.kt
package com.example.data.network.user.mapper

import com.example.data.network.user.model.UserDetailedResponse
import com.example.domain.user.model.UserDetailedData
import com.example.domain.user.model.StatsData

object UserDetailedResponseMapper {

    fun toDomain(userDetailedResponse: UserDetailedResponse): UserDetailedData {
        return UserDetailedData(
            user = UserResponseMapper.toDomain(userDetailedResponse.user),
            stats = userDetailedResponse.stats.toStatsData()
        )
    }

    private fun com.example.data.network.user.model.StatsResponse.toStatsData(): StatsData {
        return StatsData(
            walletCount = this.walletCount,
            totalTransactions = this.totalTransactions,
            expenseCount = this.expenseCount,
            incomeCount = this.incomeCount
        )
    }
}