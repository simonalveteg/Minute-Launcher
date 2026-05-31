package com.alveteg.simon.minutelauncher.home.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Onboarding(
  apps: List<AppInfo>,
  onEvent: (Event) -> Unit,
  modifier: Modifier = Modifier
) {

  val (favoriteApps, regularApps) = apps.partition { it.favorite }

  val scale = remember { Animatable(0f) }
  LaunchedEffect(favoriteApps.isNotEmpty()) {
    if (favoriteApps.isNotEmpty()) {
      scale.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessLow
        )
      )
    } else {
      scale.animateTo(0f)
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "rotation")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
    floatingActionButton = {
      if (favoriteApps.isNotEmpty() || scale.value > 0f) {
        MediumFloatingActionButton(
          onClick = {
            onEvent(HomeEvent.HideOnboarding)
          },
          shape = MaterialShapes.Cookie6Sided.toShape(),
          modifier = Modifier.graphicsLayer {
            rotationZ = rotation
            scaleX = scale.value
            scaleY = scale.value
          }
        ) {
          Icon(
            imageVector = Icons.Default.Done,
            contentDescription = null,
            modifier = Modifier.graphicsLayer { rotationZ = -rotation }
          )
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .padding(horizontal = 46.dp)
        .padding(paddingValues)
        .statusBarsPadding()
    ) {
      Text(
        text = "Favorites",
        style = MaterialTheme.typography.displayMedium,
        fontFamily = archivoBlackFamily
      )
      Text(
        text = "Select your favorite apps to get started.",
        style = MaterialTheme.typography.bodyLarge,
        fontFamily = archivoFamily
      )
      HorizontalDivider(
        modifier = Modifier.padding(top = 16.dp),
        color = MaterialTheme.colorScheme.onBackground
      )
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
      ) {
        item {
          Spacer(modifier = Modifier.height(16.dp))
          if (favoriteApps.isNotEmpty()) {
            Text(
              text = "Selected".uppercase(),
              style = MaterialTheme.typography.labelLarge,
              fontFamily = archivoBlackFamily,
            )
          }
        }
        items(
          items = favoriteApps,
          key = { it.app.packageName }
        ) { appInfo ->
          SelectableApp(
            modifier = Modifier.animateItem(),
            appInfo = appInfo
          ) { onEvent(HomeEvent.ToggleFavorite(appInfo.app)) }
        }
        item {
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Suggestions".uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontFamily = archivoBlackFamily,
          )
        }
        items(
          items = regularApps,
          key = { it.app.packageName }
        ) { appInfo ->
          SelectableApp(
            modifier = Modifier.animateItem(),
            appInfo = appInfo
          ) { onEvent(HomeEvent.ToggleFavorite(appInfo.app)) }
        }
        item {
          Spacer(Modifier.navigationBarsPadding().padding(bottom = 16.dp))
        }
      }
    }
  }
}

@Composable
private fun SelectableApp(modifier: Modifier = Modifier, appInfo: AppInfo, onClick: () -> Unit) {
  val icon = if (appInfo.favorite) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank

  Row(
    modifier = modifier
      .clickable(onClick = { onClick() })
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null
    )
    Text(
      text = appInfo.app.displayTitle ?: appInfo.app.appTitle,
      style = MaterialTheme.typography.bodyLarge,
      fontFamily = archivoFamily,
      modifier = Modifier.padding(start = 16.dp)
    )
  }
}
