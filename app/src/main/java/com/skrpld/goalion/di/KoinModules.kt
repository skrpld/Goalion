package com.skrpld.goalion.di

import androidx.room.Room
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skrpld.goalion.data.sources.local.AppDatabase
import com.skrpld.goalion.data.sources.remote.GoalRemoteDataSource
import com.skrpld.goalion.data.sources.remote.ProfileRemoteDataSource
import com.skrpld.goalion.data.sources.remote.TaskRemoteDataSource
import com.skrpld.goalion.data.sources.remote.UserRemoteDataSource
import com.skrpld.goalion.data.repositories.AuthRepositoryImpl
import com.skrpld.goalion.data.repositories.GoalRepositoryImpl
import com.skrpld.goalion.data.repositories.ProfileRepositoryImpl
import com.skrpld.goalion.data.repositories.TaskRepositoryImpl
import com.skrpld.goalion.data.repositories.UserRepositoryImpl
import com.skrpld.goalion.data.sources.remote.AuthRemoteDataSource
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
import com.skrpld.goalion.domain.usecases.GoalInteractors
import com.skrpld.goalion.domain.usecases.LogoutUseCase
import com.skrpld.goalion.domain.usecases.ProfileInteractors
import com.skrpld.goalion.domain.usecases.ReauthenticateAndSaveUseCase
import com.skrpld.goalion.domain.usecases.SignInUseCase
import com.skrpld.goalion.domain.usecases.SignUpUseCase
import com.skrpld.goalion.domain.usecases.SyncGoalUseCase
import com.skrpld.goalion.domain.usecases.SyncProfilesUseCase
import com.skrpld.goalion.domain.usecases.SyncTaskUseCase
import com.skrpld.goalion.domain.usecases.TaskInteractors
import com.skrpld.goalion.domain.usecases.UpdateGoalDescriptionUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalOrderUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalPriorityUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalStartDateUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalStatusUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalTargetDateUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalTitleUseCase
import com.skrpld.goalion.domain.usecases.UpdateGoalUseCase
import com.skrpld.goalion.domain.usecases.UpdateProfileUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskDescriptionUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskOrderUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskPriorityUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskStartDateUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskStatusUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskTargetDateUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskTitleUseCase
import com.skrpld.goalion.domain.usecases.UpdateTaskUseCase
import com.skrpld.goalion.domain.usecases.UpdateUserUseCase
import com.skrpld.goalion.domain.usecases.UserInteractors
import com.skrpld.goalion.presentation.screens.timeline.TimelineViewModel
import com.skrpld.goalion.presentation.screens.user.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val timelineModule = module {
    viewModel { TimelineViewModel(get(), get(), get()) }
    viewModel { UserViewModel(get(), get()) }
}

val domainModule = module {
    // --- Interactors ---
    factory { UserInteractors(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { ProfileInteractors(get(), get(), get(), get(), get()) }
    factory { GoalInteractors(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { TaskInteractors(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }

    // --- Auth ---
    factory { SignUpUseCase(get(), get()) }
    factory { SignInUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { ReauthenticateAndSaveUseCase(get()) }
    factory { ChangePasswordUseCase(get()) }

    // --- User ---
    factory { GetUserUseCase(get(), get()) }
    factory { UpdateUserUseCase(get(), get()) }
    factory { DeleteUserUseCase(get()) }

    // --- Profile ---
    factory { GetProfilesUseCases(get()) }
    factory { CreateProfileUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { DeleteProfileUseCase(get()) }
    factory { SyncProfilesUseCase(get()) }

    // --- Goal ---
    factory { GetGoalsWithTasksUseCase(get()) }
    factory { CreateGoalUseCase(get()) }
    factory { UpdateGoalUseCase(get()) }
    factory { DeleteGoalUseCase(get()) }
    factory { UpdateGoalStatusUseCase(get()) }
    factory { UpdateGoalPriorityUseCase(get()) }
    factory { UpdateGoalOrderUseCase(get()) }
    factory { UpdateGoalTitleUseCase(get()) }
    factory { UpdateGoalDescriptionUseCase(get()) }
    factory { UpdateGoalStartDateUseCase(get()) }
    factory { UpdateGoalTargetDateUseCase(get()) }
    factory { SyncGoalUseCase(get()) }

    // --- Task ---
    factory { CreateTaskUseCase(get()) }
    factory { UpdateTaskUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }
    factory { UpdateTaskStatusUseCase(get()) }
    factory { UpdateTaskPriorityUseCase(get()) }
    factory { UpdateTaskOrderUseCase(get()) }
    factory { UpdateTaskTitleUseCase(get()) }
    factory { UpdateTaskDescriptionUseCase(get()) }
    factory { UpdateTaskStartDateUseCase(get()) }
    factory { UpdateTaskTargetDateUseCase(get()) }
    factory { SyncTaskUseCase(get()) }
}

val dataModule = module {
    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, "goalion.db").build() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().profileDao() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().taskDao() }

    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { AuthRemoteDataSource(get()) }
    single { UserRemoteDataSource(get()) }
    single { ProfileRemoteDataSource(get()) }
    single { GoalRemoteDataSource(get()) }
    single { TaskRemoteDataSource(get()) }

    single { WorkManager.getInstance(androidContext()) }
    workerOf(::SyncWorker)

    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single<GoalRepository> { GoalRepositoryImpl(get(), get(), get()) }
    single<TaskRepository> { TaskRepositoryImpl(get(), get(), get(), get()) }
}