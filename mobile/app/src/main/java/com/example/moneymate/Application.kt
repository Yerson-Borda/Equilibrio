package com.example.moneymate

import android.app.Application
import com.example.moneymate.di.appComponent
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MoneyMateApplication  : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MoneyMateApplication )
            modules(appComponent)
        }
    }

}