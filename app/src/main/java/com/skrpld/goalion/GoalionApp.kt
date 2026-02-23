package com.skrpld.goalion

import android.app.Application
import androidx.work.Configuration
import com.skrpld.goalion.di.dataModule
import com.skrpld.goalion.di.domainModule
import com.skrpld.goalion.di.timelineModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.factory.KoinWorkerFactory
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext.startKoin

class GoalionApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GoalionApp)
            workManagerFactory()
            modules(
                timelineModule,
                domainModule,
                dataModule,
            )
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(KoinWorkerFactory())
            .build()
}