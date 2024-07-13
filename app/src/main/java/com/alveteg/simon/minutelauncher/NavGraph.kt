package com.alveteg.simon.minutelauncher

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alveteg.simon.minutelauncher.settings.GestureList
import com.alveteg.simon.minutelauncher.settings.GestureScreen
import com.alveteg.simon.minutelauncher.settings.SettingsScreen
import com.alveteg.simon.minutelauncher.settings.TimerScreen
import com.alveteg.simon.minutelauncher.utilities.Gesture

@Composable
fun LauncherNavHost(
  navController: NavHostController,
  startDestination: String
) {
  NavHost(
    navController = navController,
    startDestination = startDestination
  ) {
    composable(route = SettingsScreen.GESTURE_SETTINGS) {
      GestureScreen(onNavigate = { navController.navigationEvent(event = it) })
    }
    composable(route = SettingsScreen.GESTURE_SETTINGS_LIST + "/{gesture}") { backStackEntry ->
      val gesture = backStackEntry.arguments?.getString("gesture") ?: Gesture.NONE.toString()
      GestureList(
        onNavigate = { navController.navigationEvent(event = it) },
        gesture = Gesture.valueOf(gesture)
      )
    }
    composable(route = SettingsScreen.TIMER_SETTINGS) {
      TimerScreen(onNavigate = { navController.navigationEvent(event = it) })
    }
  }
}


fun NavController.navigationEvent(event: UiEvent.Navigate) {
  navigate(event.route) {
    if (event.popBackStack) currentDestination?.route?.let { popUpTo(it) { inclusive = true } }
    launchSingleTop = true
  }
}