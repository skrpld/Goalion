package com.skrpld.goalion.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skrpld.goalion.presentation.screens.SplashScreen
import com.skrpld.goalion.presentation.screens.timeline.TimelineScreen
import com.skrpld.goalion.presentation.screens.timeline.TimelineViewModel
import com.skrpld.goalion.presentation.screens.user.UserScreen
import com.skrpld.goalion.presentation.screens.user.UserViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Получаем ViewModel здесь, чтобы Splash мог знать о состоянии загрузки
    val userViewModel: UserViewModel = koinViewModel()
    val userState by userViewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = "splash") {

        // Экран загрузки
        composable("splash") {
            // Ждем завершения проверки токена (init блок во ViewModel)
            LaunchedEffect(userState.isLoading) {
                // Как только загрузка завершилась (isLoading == false), переходим на UserScreen
                if (!userState.isLoading) {
                    navController.navigate("user") {
                        // Убираем Splash из стека, чтобы нельзя было вернуться назад
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            SplashScreen()
        }

        // Экран пользователя (Авторизация ИЛИ Список профилей)
        composable("user") {
            UserScreen(
                viewModel = userViewModel,
                onProfileClick = { profileId ->
                    // Навигация к таймлайну конкретного профиля
                    navController.navigate("timeline/$profileId")
                }
            )
        }

        // Экран таймлайна с передачей ID
        composable(
            route = "timeline/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Koin сам подставит нужную ViewModel, если она нужна
            val timelineViewModel: TimelineViewModel = koinViewModel()

            // Если нужно передать ID во ViewModel:
            val profileId = backStackEntry.arguments?.getString("profileId")
            // timelineViewModel.loadData(profileId)

            TimelineScreen(viewModel = timelineViewModel)
        }
    }
}