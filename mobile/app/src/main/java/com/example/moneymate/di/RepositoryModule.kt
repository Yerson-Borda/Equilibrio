package com.example.moneymate.di

import com.example.domain.accessToken.AccessTokenRepository
import com.example.domain.accessToken.AccessTokenRepositoryImpl
import com.example.domain.auth.AuthRepository
import com.example.domain.auth.AuthRepositoryImpl
import com.example.data.network.user.UserRepositoryImpl
import com.example.data.network.wallet.WalletRepositoryImpl
import com.example.domain.user.UserRepository
import com.example.domain.transaction.TransactionRepository
import com.example.data.network.transaction.TransactionRepositoryImpl
import com.example.domain.category.CategoryRepository
import com.example.data.network.category.CategoryRepositoryImpl
import com.example.data.network.budget.BudgetRepositoryImpl
import com.example.domain.budget.BudgetRepository
import com.example.domain.tag.TagRepository
import com.example.data.network.tag.TagRepositoryImpl
import com.example.domain.categoryLimit.CategoryLimitRepository
import com.example.data.network.categoryLimit.CategoryLimitRepositoryImpl
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
    factoryOf(::CategoryRepositoryImpl) {bind<CategoryRepository>()}
    factoryOf(::BudgetRepositoryImpl) {bind<BudgetRepository>()}
    factoryOf(::TagRepositoryImpl) {bind<TagRepository>()}
    factoryOf(::CategoryLimitRepositoryImpl) {bind<CategoryLimitRepository>()}
}