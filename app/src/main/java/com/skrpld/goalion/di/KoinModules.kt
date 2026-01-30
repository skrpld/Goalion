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
import com.skrpld.goalion.domain.usecases.ChangePasswordUseCase
import com.skrpld.goalion.domain.usecases.CreateGoalUseCase
import com.skrpld.goalion.domain.usecases.CreateProfileUseCase
import com.skrpld.goalion.domain.usecases.CreateTaskUseCase
import com.skrpld.goalion.domain.usecases.DeleteGoalUseCase
import com.skrpld.goalion.domain.usecases.DeleteProfileUseCase
import com.skrpld.goalion.domain.usecases.DeleteTaskUseCase
import com.skrpld.goalion.domain.usecases.DeleteUserUseCase
import com.skrpld.goalion.domain.usecases.GetGoalsWithTasksUseCase
import com.skrpld.goalion.domain.usecases.GetProfilesUseCases
import com.skrpld.goalion.domain.usecases.GetUserUseCase
import com.skrpld.goalion.domain.usecases.LogoutUseCase
import com.skrpld.goalion.domain.usecases.ReauthenticateAndSaveUseCase
import com.skrpld.goalion.domain.usecases.SignInUseCase
import com.skrpld.goalion.domain.usecases.SignUpUseCase
import com.skrpld.goalion.domain.usecases.SyncGoalUseCase
import com.skrpld.goalion.domain.usecases.SyncProfilesUseCase
import com.skrpld.goalion.domain.usecases.SyncTaskUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalOrderUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalPriorityUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalStatusUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalUseCase
import com.skrpld.goalion.domain.usecases.UpdateProfileUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskOrderUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskPriorityUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskStatusUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskUseCase
import com.skrpld.goalion.domain.usecases.UpdateUserUseCase
import com.skrpld.goalion.ui.screens.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { HomeViewModel() }
}

val domainModule = module {
    factory { SignUpUseCase(get(), get()) }
    factory { SignInUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { ReauthenticateAndSaveUseCase(get()) }
    factory { ChangePasswordUseCase(get()) }

    factory { GetUserUseCase(get(), get()) }
    factory { UpdateUserUseCase(get()) }
    factory { DeleteUserUseCase(get()) }

    factory { GetProfilesUseCases(get()) }
    factory { CreateProfileUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { DeleteProfileUseCase(get()) }
    factory { SyncProfilesUseCase(get()) }

    factory { GetGoalsWithTasksUseCase(get()) }
    factory { CreateGoalUseCase(get()) }
    factory { UpdateGoalUseCase(get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { UpdateGoalStatusUseCase(get()) }
    factory { UpdateGoalPriorityUseCase(get()) }
    factory { UpdateGoalOrderUseCase(get()) }
    factory { SyncGoalUseCase(get()) }

    factory { CreateTaskUseCase(get()) }
    factory { UpdateTaskUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }
    factory { UpdateTaskStatusUseCase(get()) }
    factory { UpdateTaskPriorityUseCase(get()) }
    factory { UpdateTaskOrderUseCase(get()) }
    factory { SyncTaskUseCase(get()) }
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