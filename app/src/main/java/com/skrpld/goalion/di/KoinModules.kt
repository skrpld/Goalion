package com.skrpld.goalion.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.skrpld.goalion.data.local.AppDatabase
import com.skrpld.goalion.data.remote.GoalRemoteDataSource
import com.skrpld.goalion.data.remote.ProfileRemoteDataSource
import com.skrpld.goalion.data.remote.TaskRemoteDataSource
import com.skrpld.goalion.data.remote.UserRemoteDataSource
import com.skrpld.goalion.data.workers.SyncWorker
import com.skrpld.goalion.ui.screens.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel() }
}

val localModule = module {
    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, "goalion.db").build() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().profileDao() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().taskDao() }
}

val remoteModule = module {
    single { FirebaseFirestore.getInstance() }
    single { UserRemoteDataSource(get()) }
    single { ProfileRemoteDataSource(get()) }
    single { GoalRemoteDataSource(get()) }
    single { TaskRemoteDataSource(get()) }

    worker { SyncWorker(get(), get(),
        get(), get(), get(),
        get(), get(), get()) }
}