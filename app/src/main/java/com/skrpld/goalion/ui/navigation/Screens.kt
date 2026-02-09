package com.skrpld.goalion.ui.navigation

sealed class Screens(val route: String) {
    object Splash : Screens("splash")

    object Timeline : Screens("timeline")
    object User : Screens("user")
    object Settings : Screens("settings")
}