package com.example.moneymate.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.moneymate.ui.screens.auth.signUp.SignUpViewModel
import com.example.moneymate.ui.screens.auth.singIn.SignInViewModel
import com.example.moneymate.ui.screens.home.HomeViewModel
import com.example.moneymate.ui.screens.profile.editprofile.EditProfileViewModel
import com.example.moneymate.ui.screens.profile.profileoptions.ProfileOptionsScreenViewModel
import com.example.moneymate.ui.screens.profile.settings.SettingsScreenViewModel
import com.example.moneymate.ui.screens.transaction.TransactionScreenViewModel
import com.example.moneymate.ui.screens.transaction.addtransaction.AddTransactionViewModel
import com.example.moneymate.ui.screens.wallet.WalletViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


@RequiresApi(Build.VERSION_CODES.O)
val viewModelModule  = module {
    viewModel { SignUpViewModel(get()) }
    viewModel{SignInViewModel(get())}
    viewModel{ HomeViewModel(get(), get(), get(), get()) }
    viewModel{ WalletViewModel(get(), get(), get(), get(),get(), get()) }
    viewModel{ AddTransactionViewModel(get(), get(), get(), get() , get()) }
    viewModel { EditProfileViewModel(get(), get(), get(), get()) }
    viewModel { ProfileOptionsScreenViewModel(get()) }
    viewModel { SettingsScreenViewModel(get(), get()) }
    viewModel{ TransactionScreenViewModel(get(), get() , get() , get()) }
}