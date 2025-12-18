package com.skrpld.goalion

import android.app.Application
import androidx.room.Room
import com.skrpld.goalion.data.GoalRepository
import com.skrpld.goalion.data.database.AppDatabase
import com.skrpld.goalion.ui.screens.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

class GoalionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@GoalionApp)
            modules(appModule)
        }
    }
}

val appModule = module {
    single { Room.databaseBuilder(get(), AppDatabase::class.java, "goalion.db").build() }
    single { get<AppDatabase>().appDao() }

    single { GoalRepository(get()) }

    viewModel { MainViewModel(get()) }
}