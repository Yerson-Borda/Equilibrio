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
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@MoneyMateApplication )
            // Load modules
            modules(appComponent)
        }
    }

}