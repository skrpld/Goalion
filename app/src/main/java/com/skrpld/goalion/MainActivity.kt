package com.skrpld.goalion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.skrpld.goalion.presentation.navigation.AppNavigation
import com.skrpld.goalion.presentation.theme.GoalionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoalionTheme {
                AppNavigation()
            }
        }
    }
}