package com.example.moneymate.di

import com.example.domain.auth.dataSource.AuthRemoteDataSource
import com.example.data.network.auth.AuthRemoteDataSourceImpl
import com.example.data.dataStore.DataStoreDataSourceImpl
import org.koin.core.module.dsl.factoryOf
import com.example.domain.auth.dataStore.DataStoreDataSource
import org.koin.core.module.dsl.bind
import org.koin.dsl.module

val dataSourceModule = module {
    factoryOf(::DataStoreDataSourceImpl) { bind<DataStoreDataSource>() }
    factoryOf(::AuthRemoteDataSourceImpl) { bind<AuthRemoteDataSource>()}
}