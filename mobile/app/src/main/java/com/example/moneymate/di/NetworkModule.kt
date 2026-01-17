package com.example.moneymate.di

import com.example.data.network.auth.AuthApi
import com.example.data.network.budget.BudgetApi
import com.example.data.network.category.CategoryApi
import com.example.data.network.categoryLimit.CategoryLimitApi
import com.example.data.network.common.Network
import com.example.data.network.tag.TagApi
import com.example.data.network.user.UserApi
import com.example.data.network.transaction.TransactionApi
import com.example.data.network.wallet.WalletApi
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
    single<UserApi> { Network.getApi(get()) }
    single<WalletApi> { Network.getApi(get()) }
    single<TransactionApi> {Network.getApi(get())}
    single<CategoryApi> {Network.getApi(get())}
    single<BudgetApi> {Network.getApi(get())}
    single<TagApi>{Network.getApi(get())}
    single<CategoryLimitApi>{Network.getApi(get())}
}