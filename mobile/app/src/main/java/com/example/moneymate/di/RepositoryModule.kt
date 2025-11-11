package com.example.moneymate.di

import com.example.domain.accessToken.AccessTokenRepository
import com.example.domain.accessToken.AccessTokenRepositoryImpl
import com.example.domain.auth.AuthRepository
import com.example.domain.auth.AuthRepositoryImpl
import com.example.data.network.home.UserRepositoryImpl
import com.example.data.network.wallet.WalletRepositoryImpl
import com.example.domain.home.UserRepository
import com.example.domain.transaction.TransactionRepository
import com.example.data.network.transaction.TransactionRepositoryImpl
import com.example.domain.wallet.WalletRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.koin.core.module.dsl.bind

val repositoryModule = module {
    factoryOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    factoryOf(::AccessTokenRepositoryImpl) { bind<AccessTokenRepository>()}
    factoryOf(::UserRepositoryImpl) {bind<UserRepository>()}
    factoryOf(::WalletRepositoryImpl) {bind<WalletRepository>()}
    factoryOf(::TransactionRepositoryImpl) {bind<TransactionRepository>()}
}