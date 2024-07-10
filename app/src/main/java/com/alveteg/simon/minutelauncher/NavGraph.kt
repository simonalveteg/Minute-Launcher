package com.alveteg.simon.minutelauncher

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alveteg.simon.minutelauncher.home.HomeScreen
import com.alveteg.simon.minutelauncher.settings.GestureList
import com.alveteg.simon.minutelauncher.settings.GestureScreen
import com.alveteg.simon.minutelauncher.settings.TimerScreen
import com.alveteg.simon.minutelauncher.utilities.Gesture

@Composable
fun LauncherNavHost(
  navController: NavHostController,
) {
  NavHost(navController = navController,
    startDestination = MinuteRoute.HOME,
    enterTransition = { EnterTransition.None },
    exitTransition = { fadeOut(tween(delayMillis = 2000)) },
    popEnterTransition = { EnterTransition.None },
    popExitTransition = { ExitTransition.None }) {
    composable(
      route = MinuteRoute.HOME
    ) {
      HomeScreen(onNavigate = { navController.navigationEvent(event = it) })
    }
    composable(route = MinuteRoute.GESTURE_SETTINGS,
      enterTransition = {
        slideIntoContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Up
        )
      },
      exitTransition = { fadeOut() },
      popEnterTransition = { fadeIn() },
      popExitTransition = { fadeOut() }) {
      GestureScreen(onNavigate = { navController.navigationEvent(event = it) })
    }
    composable(
      route = MinuteRoute.GESTURE_SETTINGS_LIST + "/{gesture}",
      enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) }
    ) { backStackEntry ->
      val gesture = backStackEntry.arguments?.getString("gesture") ?: Gesture.NONE.toString()
      GestureList(
        onNavigate = { navController.navigationEvent(event = it) },
        gesture = Gesture.valueOf(gesture)
      )
    }
    composable(
      route = MinuteRoute.TIMER_SETTINGS,
      enterTransition = {
        slideIntoContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Up
        )
      },
      exitTransition = { fadeOut() },
      popEnterTransition = { fadeIn() },
      popExitTransition = { fadeOut() }
    ) {
      TimerScreen(onNavigate = { navController.navigationEvent(event = it) })
    }
  }
}

object MinuteRoute {
  const val HOME = "home"
  const val GESTURE_SETTINGS = "gesture_settings"
  const val GESTURE_SETTINGS_LIST = "gestures_list"
  const val TIMER_SETTINGS = "timer_settings"
}

fun NavController.navigationEvent(event: UiEvent.Navigate) {
  navigate(event.route) {
    if (event.popBackStack) currentDestination?.route?.let { popUpTo(it) { inclusive = true } }
    launchSingleTop = true
  }
}