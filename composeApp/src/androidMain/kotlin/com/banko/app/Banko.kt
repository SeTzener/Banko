package com.banko.app

import android.app.Application
import com.banko.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class Banko: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@Banko)
        }
    }
}