package com.skrpld.goalion

import android.app.Application
import com.skrpld.goalion.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class GoalionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GoalionApp)
            modules(appModule)
        }
    }
}