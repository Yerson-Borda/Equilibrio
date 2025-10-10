package com.example.moneymate.di

import com.example.moneymate.ui.screens.auth.signUp.SignUpViewModel
import com.example.moneymate.ui.screens.auth.singIn.SignInViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule  = module {
    viewModel { SignUpViewModel(get()) }
    viewModel{SignInViewModel(get())}
}
