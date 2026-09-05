package com.alveteg.simon.minutelauncher.home.onboarding

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppCategory
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(
  ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
  ExperimentalSharedTransitionApi::class
)
@Composable
fun SharedTransitionScope.FavoriteSelectionScreen(
  apps: List<AppInfo>,
  animatedVisibilityScope: AnimatedVisibilityScope,
  onNext: () -> Unit,
  onEvent: (Event) -> Unit
) {
  var isExiting by remember { mutableStateOf(false) }
  val exitScale = remember { Animatable(1f) }

  LaunchedEffect(isExiting) {
    if (isExiting) {
      launch {
        delay(400)
        onNext()
      }
      exitScale.animateTo(
        targetValue = 50f,
        animationSpec = tween(durationMillis = 600)
      )
    }
  }

  val (favoriteApps, regularApps) = apps.partition { it.favorite }

  val suggestedPackageNames = remember(apps.map { it.app.packageName }) {
    val initialRegular = apps.filter { !it.favorite }
    val scored = initialRegular.map { appInfo ->
      val app = appInfo.app
      var score = 0

      score += when (app.category) {
        AppCategory.SOCIAL -> 10
        AppCategory.PRODUCTIVITY -> 9
        AppCategory.MAPS -> 8
        AppCategory.AUDIO -> 7
        AppCategory.NEWS -> 6
        AppCategory.VIDEO -> 5
        AppCategory.IMAGE -> 4
        AppCategory.ACCESSIBILITY -> 3
        AppCategory.GAME -> 1
        else -> 0
      }

      if (appInfo.isSystemApp && !appInfo.isUpdatedSystemApp) score -= 15
      if (appInfo.isDefaultBrowser || appInfo.isDefaultSms || appInfo.isDefaultDialer) score += 20
      if (app.category == AppCategory.SOCIAL && !appInfo.canHandleShare) score -= 8

      appInfo to score
    }

    val categoryLimits = mapOf(
      AppCategory.SOCIAL to 3,
      AppCategory.PRODUCTIVITY to 3,
      AppCategory.MAPS to 1,
      AppCategory.AUDIO to 2,
      AppCategory.NEWS to 1,
      AppCategory.VIDEO to 1,
      AppCategory.IMAGE to 1,
      AppCategory.GAME to 1
    )
    val defaultLimit = 1

    scored
      .asSequence()
      .filter { it.second >= 5 }
      .groupBy { it.first.app.category }
      .asSequence()
      .flatMap { (category, appsInCategory) ->
        val limit = categoryLimits[category] ?: defaultLimit
        appsInCategory
          .sortedByDescending { it.second }
          .take(limit)
      }
      .map { it.first }
      .sortedByDescending { app -> scored.find { it.first == app }?.second ?: 0 }
      .take(16)
      .map { it.app.packageName }
      .toList()
  }

  val suggestedApps = remember(regularApps, suggestedPackageNames) {
    suggestedPackageNames.mapNotNull { pkg -> regularApps.find { it.app.packageName == pkg } }
  }
  val otherApps = regularApps.filter { it.app.packageName !in suggestedPackageNames }

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

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.Start
  ) {
    Text(
      text = "Favorites",
      style = MaterialTheme.typography.displayLargeEmphasized,
      modifier = Modifier
        .padding(bottom = 4.dp)
        .padding(horizontal = 24.dp)
    )
    Text(
      text = "Select your most important apps to get started.",
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier
        .padding(bottom = 20.dp)
        .padding(horizontal = 24.dp)
    )

    Surface(
      modifier = Modifier.padding(horizontal = 16.dp),
      shape = MaterialTheme.shapes.extraLarge.copy(
        bottomEnd = CornerSize(0.dp),
        bottomStart = CornerSize(0.dp)
      )
    ) {
      LazyColumn(
        modifier = Modifier
          .padding(horizontal = 24.dp)
      ) {
        if (favoriteApps.isNotEmpty()) {
          item(key = "favoritesHeader") { SectionHeader("Favorites") }
          itemsIndexed(
            items = favoriteApps,
            key = { _, appInfo -> appInfo.app.packageName }
          ) { index, appInfo ->
            SelectableApp(
              modifier = Modifier.animateItem(),
              appInfo = appInfo,
              shape = verticalItemShape(index, favoriteApps.size)
            ) {
              onEvent(HomeEvent.ToggleFavorite(appInfo.app))
            }
          }
        }
        if (suggestedApps.isNotEmpty()) {
          item(key = "suggestionsHeader") { SectionHeader("Suggestions") }
          itemsIndexed(
            items = suggestedApps,
            key = { _, appInfo -> appInfo.app.packageName }
          ) { index, appInfo ->
            SelectableApp(
              modifier = Modifier.animateItem(),
              appInfo = appInfo,
              shape = verticalItemShape(index, suggestedApps.size)
            ) {
              onEvent(HomeEvent.ToggleFavorite(appInfo.app))
            }
          }
        }

        if (otherApps.isNotEmpty()) {
          item(key = "otherAppsHeader") { SectionHeader("Other apps") }
          itemsIndexed(
            items = otherApps,
            key = { _, appInfo -> appInfo.app.packageName }
          ) { index, appInfo ->
            SelectableApp(
              modifier = Modifier.animateItem(),
              appInfo = appInfo,
              shape = verticalItemShape(index, otherApps.size)
            ) {
              onEvent(HomeEvent.ToggleFavorite(appInfo.app))
            }
          }
        }
        item {
          Spacer(
            modifier = Modifier.navigationBarsPadding()
          )
        }
      }
    }
  }
}

@Composable
private fun SelectableApp(
  modifier: Modifier = Modifier,
  appInfo: AppInfo,
  shape: Shape = RectangleShape,
  onClick: () -> Unit
) {
  val icon = if (appInfo.favorite) Icons.Filled.Star else Icons.Filled.StarBorder

  Surface(
    modifier = modifier.padding(vertical = 1.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
    shape = shape
  ) {
    Box(
      modifier = Modifier
        .clickable(onClick = { onClick() })
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 8.dp),
    ) {
      Text(
        text = appInfo.app.title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.align(Alignment.Center)
      )
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 8.dp)
      )
    }
  }
}

@Composable
private fun LazyItemScope.SectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelLargeEmphasized,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier
      .animateItem()
      .padding(top = 16.dp, bottom = 8.dp)
  )
}

private fun verticalItemShape(index: Int, count: Int): Shape {
  val radius = 16.dp
  return when {
    count == 1 -> RoundedCornerShape(radius)
    index == 0 -> RoundedCornerShape(topStart = radius, topEnd = radius)
    index == count - 1 -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
    else -> RoundedCornerShape(2.dp)
  }
}