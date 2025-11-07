package com.example.moneymate.di

import com.example.moneymate.ui.screens.auth.signUp.SignUpViewModel
import com.example.moneymate.ui.screens.auth.singIn.SignInViewModel
import com.example.moneymate.ui.screens.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule  = module {
    viewModel { SignUpViewModel(get()) }
    viewModel{SignInViewModel(get())}
    viewModel{ HomeViewModel(get()) }
}
