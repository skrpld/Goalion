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

    val userViewModel: UserViewModel = koinViewModel()
    val userState by userViewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            LaunchedEffect(userState.isLoading) {
                if (!userState.isLoading) {
                    navController.navigate("user") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            SplashScreen()
        }

        composable("user") {
            UserScreen(
                viewModel = userViewModel,
                onProfileClick = { profileId ->
                    navController.navigate("timeline/$profileId")
                }
            )
        }

        composable(
            route = "timeline/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val timelineViewModel: TimelineViewModel = koinViewModel()

            val profileId = backStackEntry.arguments?.getString("profileId")

            TimelineScreen(viewModel = timelineViewModel)
        }
    }
}