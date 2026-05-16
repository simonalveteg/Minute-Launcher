package com.alveteg.simon.minutelauncher.settings

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alveteg.simon.minutelauncher.UiEvent
import com.alveteg.simon.minutelauncher.utilities.Gesture

@Composable
fun SettingsNavHost(
  navController: NavHostController,
  startDestination: String
) {
  NavHost(
    navController = navController,
    startDestination = startDestination
  ) {
    composable(route = SettingsScreen.HOME) {
      SettingsScreen(onNavigate = { navController.navigationEvent(event = it) })
    }
    composable(route = SettingsScreen.GESTURE_SETTINGS_LIST + "/{gesture}") { backStackEntry ->
      val gesture = backStackEntry.arguments?.getString("gesture") ?: Gesture.NONE.toString()
      GestureList(
        onNavigate = { navController.navigationEvent(event = it) },
        gesture = Gesture.valueOf(gesture)
      )
    }
  }
}


fun NavController.navigationEvent(event: UiEvent.Navigate) {
  if (event.route == SettingsScreen.BACK) {
    (context as? Activity)?.finish()
    return
  }

  navigate(event.route) {
    if (event.popBackStack) currentDestination?.route?.let { popUpTo(it) { inclusive = true } }
    launchSingleTop = true
  }
}