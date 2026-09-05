package com.alveteg.simon.minutelauncher.home.onboarding

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent

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
      modifier = Modifier.fillMaxSize(),
      enterTransition = { fadeIn(animationSpec = tween(600)) },
      exitTransition = { fadeOut(animationSpec = tween(600)) }
    ) {
      composable("app_selection") {
        FavoriteSelectionScreen(
          apps = apps,
          animatedVisibilityScope = this,
          onNext = { navController.navigate("permissions") },
          onEvent = onEvent
        )
      }
      composable("permissions") {
        PermissionsScreen(
          animatedVisibilityScope = this,
          onFinish = { onEvent(HomeEvent.HideOnboarding) },
          onEvent = { onEvent(it) }
        )
      }
    }
  }
}

