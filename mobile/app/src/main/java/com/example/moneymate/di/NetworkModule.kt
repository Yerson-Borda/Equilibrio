package com.example.moneymate.di

import com.example.data.network.auth.AuthApi
import com.example.data.network.common.Network
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    factory { Network.okHttpCache }
    single { Network.appJson }
    factoryOf(Network::getJsonFactory)
    factoryOf(Network::getLoggingInterceptor)
    factoryOf(Network::getHeadersInterceptor)
    factoryOf(Network::getRefreshTokenAuthenticator)
    singleOf(Network::getHttpClient)
    singleOf(Network::getRetrofit)
    // apis
    single<AuthApi> { Network.getApi(get()) }
}