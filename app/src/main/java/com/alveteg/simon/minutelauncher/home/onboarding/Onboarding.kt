package com.alveteg.simon.minutelauncher.home.onboarding

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.home.onboarding.PermissionsScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Onboarding(
  apps: List<AppInfo>,
  onEvent: (Event) -> Unit,
  modifier: Modifier = Modifier
) {
  val navController = rememberNavController()

  SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
    NavHost(
      navController = navController,
      startDestination = "permissions",
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
      enterTransition = {
        fadeIn() + slideInHorizontally { it / 2 }
      },
      exitTransition = {
        fadeOut() + slideOutHorizontally { it / 2 }
      },
      popEnterTransition = {
        fadeIn() + slideInHorizontally()
      },
      popExitTransition = {
        fadeOut() + slideOutHorizontally()
      }
    ) {
      composable("app_selection") {
        FavoriteSelectionScreen(
          apps = apps,
          animatedVisibilityScope = this,
          onNext = { onEvent(HomeEvent.HideOnboarding) },
          onEvent = onEvent
        )
      }
      composable("permissions") {
        PermissionsScreen(
          animatedVisibilityScope = this,
          onFinish = { navController.navigate("app_selection") },
        )
      }
    }
  }
}

