package com.skrpld.goalion

import android.app.Application
import com.skrpld.goalion.di.dataModule
import com.skrpld.goalion.di.domainModule
import com.skrpld.goalion.di.presentationModule
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
                presentationModule,
                domainModule,
                dataModule,
            )
        }
    }
}