package com.skrpld.goalion

import android.app.Application
import com.skrpld.goalion.di.localModule
import com.skrpld.goalion.di.remoteModule
import com.skrpld.goalion.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin

class GoalionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GoalionApp)
            workManagerFactory()
            modules(
                viewModelModule,
                localModule,
                remoteModule
            )
        }
    }
}