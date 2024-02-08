package com.alveteg.simon.minutelauncher

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alveteg.simon.minutelauncher.home.HomeScreen

@Composable
fun LauncherNavHost(
  navController: NavHostController,
) {
  NavHost(navController = navController, startDestination = "main") {
    composable("main") {
      HomeScreen()
    }
  }
}