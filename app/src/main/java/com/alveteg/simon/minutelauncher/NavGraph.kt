package com.alveteg.simon.minutelauncher

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alveteg.simon.minutelauncher.home.HomeScreen
import com.alveteg.simon.minutelauncher.settings.GestureScreen

@Composable
fun LauncherNavHost(
  navController: NavHostController,
) {
  NavHost(
    navController = navController,
    startDestination = MinuteRoute.HOME,
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None },
    popEnterTransition = { EnterTransition.None },
    popExitTransition = { ExitTransition.None }
  ) {
    composable(
      route = MinuteRoute.HOME,
      exitTransition = { fadeOut(tween(delayMillis = 2000)) }
    ) {
      HomeScreen(navController)
    }
    composable(
      route = MinuteRoute.GESTURES,
      enterTransition = {
        slideIntoContainer(
          towards = AnimatedContentTransitionScope.SlideDirection.Up
        )
      },
      exitTransition = { fadeOut() },
      popEnterTransition = { fadeIn() },
      popExitTransition = { fadeOut() }
    ) {
      GestureScreen(navController)
    }
  }
}

object MinuteRoute {
  const val HOME = "home"
  const val GESTURES = "gestures"
}