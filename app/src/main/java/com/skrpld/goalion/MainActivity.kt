package com.skrpld.goalion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skrpld.goalion.data.database.AppDao
import com.skrpld.goalion.data.database.AppDatabase
import com.skrpld.goalion.ui.screens.main.MainScreen
import com.skrpld.goalion.ui.screens.main.MainViewModel
import com.skrpld.goalion.ui.screens.main.MainViewModelFactory
import com.skrpld.goalion.ui.theme.GoalionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoalionTheme {
                // 1. Получаем контекст
                val context = LocalContext.current

                // 2. Получаем инстанс БД и DAO
                val database = AppDatabase.getDatabase(context)
                val dao = database.appDao()

                // 3. Создаем ViewModel с помощью фабрики
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(dao)
                )

                // 4. Передаем готовую ViewModel в экран
                MainScreen(viewModel = viewModel)
            }
        }
    }
}