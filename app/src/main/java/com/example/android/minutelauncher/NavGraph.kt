package com.example.android.minutelauncher

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun LauncherNavHost(
    navController: NavHostController,
    viewModel: LauncherViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            BackHandler(true) {
                viewModel.onEvent(Event.CloseAppsList)
            }
            MainScreen()
        }
    }
}