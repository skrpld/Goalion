package com.skrpld.goalion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skrpld.goalion.ui.screens.SplashScreen
import com.skrpld.goalion.ui.screens.timeline.TimelineScreen
import com.skrpld.goalion.ui.screens.timeline.TimelineViewModel
import com.skrpld.goalion.ui.screens.user.UserScreen
import com.skrpld.goalion.ui.screens.user.UserViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val userViewModel: UserViewModel = koinViewModel()
    val authState = userViewModel.authState

    val firstRoute = "timeline/custom_id" // if (authState.collectAsState().value == AuthState.LoggedIn) "timeline" else "user"

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            LaunchedEffect(Unit) {
                delay(1500)
                navController.navigate(firstRoute) {
                    popUpTo("splash") { inclusive = true }
                }
            }

            SplashScreen()
        }

        composable("timeline/{profileId}") { backStackEntry ->
            val viewModel: TimelineViewModel = koinViewModel()
            TimelineScreen(viewModel = viewModel)
        }

        composable("user") {
            UserScreen(viewModel = userViewModel)
        }
    }
}