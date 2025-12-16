package com.skrpld.goalion.ui.navigation

import androidx.navigation.NavHostController

class AppNavigation(
    private val navController: NavHostController
) {
    fun navigateToProfile() {
        navController.navigate(AppDestinations.PROFILE_ROUTE)
    }

    fun navigateToHome() {
        navController.navigate(AppDestinations.HOME_ROUTE)
    }
}