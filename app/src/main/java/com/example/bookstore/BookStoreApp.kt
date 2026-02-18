package com.example.bookstore

import android.app.Application
import com.example.bookstore.data.prefs.ThemeStartupInitializer
import com.example.bookstore.di.appModule
import com.example.bookstore.worker.SyncScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BookStoreApp : Application() {
    override fun onCreate() {
        ThemeStartupInitializer.initialize(this)
        super.onCreate()
        startKoin {
            androidContext(this@BookStoreApp)
            modules(appModule)
        }
        SyncScheduler.schedule(this)
    }
}
