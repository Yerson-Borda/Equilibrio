package com.example.moneymate.di

import com.example.domain.accessToken.AccessTokenRepository
import com.example.domain.accessToken.AccessTokenRepositoryImpl
import com.example.domain.auth.AuthRepository
import com.example.domain.auth.AuthRepositoryImpl
import com.example.domain.home.HomeRepository
import com.example.data.network.home.HomeRepositoryImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.koin.core.module.dsl.bind

val repositoryModule = module {
    factoryOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    factoryOf(::AccessTokenRepositoryImpl) { bind<AccessTokenRepository>()}
    factoryOf(::HomeRepositoryImpl) {bind<HomeRepository>()}
}