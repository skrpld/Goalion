package com.skrpld.goalion.di

import androidx.room.Room
import com.skrpld.goalion.data.local.AppDatabase
import com.skrpld.goalion.ui.screens.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, "goalion.db").build() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().profileDao() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().taskDao() }

    viewModel { MainViewModel(get()) }
}