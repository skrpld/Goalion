package com.skrpld.goalion.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.skrpld.goalion.data.local.AppDatabase
import com.skrpld.goalion.data.remote.GoalRemoteDataSource
import com.skrpld.goalion.data.remote.ProfileRemoteDataSource
import com.skrpld.goalion.data.remote.TaskRemoteDataSource
import com.skrpld.goalion.data.remote.UserRemoteDataSource
import com.skrpld.goalion.data.repositories.AuthRepositoryImpl
import com.skrpld.goalion.data.repositories.GoalRepositoryImpl
import com.skrpld.goalion.data.repositories.ProfileRepositoryImpl
import com.skrpld.goalion.data.repositories.TaskRepositoryImpl
import com.skrpld.goalion.data.repositories.UserRepositoryImpl
import com.skrpld.goalion.data.workers.SyncWorker
import com.skrpld.goalion.domain.repositories.AuthRepository
import com.skrpld.goalion.domain.repositories.GoalRepository
import com.skrpld.goalion.domain.repositories.ProfileRepository
import com.skrpld.goalion.domain.repositories.TaskRepository
import com.skrpld.goalion.domain.repositories.UserRepository
import com.skrpld.goalion.ui.screens.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { HomeViewModel() }
}

val dataModule = module {
    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, "goalion.db").build() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().profileDao() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().taskDao() }

    single { FirebaseFirestore.getInstance() }
    single { UserRemoteDataSource(get()) }
    single { ProfileRemoteDataSource(get()) }
    single { GoalRemoteDataSource(get()) }
    single { TaskRemoteDataSource(get()) }

    worker { SyncWorker(get(), get(), get(), get(), get(), get(), get(), get()) }

    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single<GoalRepository> { GoalRepositoryImpl(get(), get(), get()) }
    single<TaskRepository> { TaskRepositoryImpl(get(), get(), get(), get()) }
}