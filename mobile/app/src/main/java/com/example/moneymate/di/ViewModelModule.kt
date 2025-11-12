package com.example.moneymate.di

import com.example.moneymate.ui.screens.auth.signUp.SignUpViewModel
import com.example.moneymate.ui.screens.auth.singIn.SignInViewModel
import com.example.moneymate.ui.screens.home.HomeViewModel
import com.example.moneymate.ui.screens.profile.editprofile.EditProfileViewModel
import com.example.moneymate.ui.screens.transaction.AddTransactionViewModel
import com.example.moneymate.ui.screens.wallet.WalletViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule  = module {
    viewModel { SignUpViewModel(get()) }
    viewModel{SignInViewModel(get())}
    viewModel{ HomeViewModel(get(), get()) }
    viewModel{ WalletViewModel(get(), get(), get(), get(),get(), get()) }
    viewModel{ AddTransactionViewModel(get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get(), get(), get()) }
}