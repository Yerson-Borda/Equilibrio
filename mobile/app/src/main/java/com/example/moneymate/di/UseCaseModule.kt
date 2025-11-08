package com.example.moneymate.di

import com.example.domain.auth.usecase.IsUserSignedInUseCase
import com.example.domain.auth.usecase.IsUserSignedInUseCaseImpl
import com.example.domain.auth.usecase.SignUpUseCase
import com.example.domain.auth.usecase.SignUpUseCaseImpl
import com.example.domain.auth.usecase.SignInUseCase
import com.example.domain.auth.usecase.SignInUseCaseImpl
import com.example.domain.home.usecase.GetUserDetailedUseCase
import com.example.domain.wallet.usecase.CreateWalletUseCase
import com.example.domain.wallet.usecase.GetTotalBalanceUseCase
import com.example.domain.wallet.usecase.GetWalletTransactionsUseCase
import com.example.domain.wallet.usecase.GetWalletUseCase
import com.example.domain.wallet.usecase.GetWalletsUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCaseModule = module {
    factoryOf(::IsUserSignedInUseCaseImpl) { bind<IsUserSignedInUseCase>() }
    factoryOf(::SignUpUseCaseImpl) { bind<SignUpUseCase>() }
    factoryOf(::SignInUseCaseImpl) { bind<SignInUseCase>() }
    factory { GetUserDetailedUseCase(get()) }
    factory { GetWalletUseCase(get()) }
    factory { GetWalletsUseCase(get())}
    factory { CreateWalletUseCase(get()) }
    factory { GetWalletTransactionsUseCase(get()) }
    factory { GetTotalBalanceUseCase(get()) }
}