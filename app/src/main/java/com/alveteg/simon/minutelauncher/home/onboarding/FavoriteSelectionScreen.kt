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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.AppCategory
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2


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

  val iconAlpha by animateFloatAsState(
    targetValue = if (isExiting) 0f else 1f,
    animationSpec = tween(durationMillis = 100), label = "icon_alpha"
  )

  val fabColor by animateColorAsState(
    targetValue = if (isExiting) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer,
    animationSpec = tween(durationMillis = 1000),
    label = "fab_color"
  )

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

  val listState = rememberLazyListState()
  val density = LocalDensity.current
  val scrollProgress by remember {
    derivedStateOf {
      if (listState.firstVisibleItemIndex > 0) 1f
      else {
        val threshold = with(density) { 192.dp.toPx() }
        (listState.firstVisibleItemScrollOffset / threshold).coerceIn(0f, 1f)
      }
    }
  }


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
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
    floatingActionButton = {
      if (favoriteApps.isNotEmpty() || scale.value > 0f) {
        MediumFloatingActionButton(
          onClick = { isExiting = true },
          containerColor = fabColor,
          shape = MaterialShapes.Cookie6Sided.toShape(),
          modifier = Modifier
            .sharedElement(
              rememberSharedContentState(key = "fab"),
              animatedVisibilityScope = animatedVisibilityScope
            )
            .graphicsLayer {
              rotationZ = rotation
              scaleX = scale.value * exitScale.value
              scaleY = scale.value * exitScale.value
            }
        ) {
          Icon(
            imageVector = Icons.Default.Done,
            contentDescription = null,
            modifier = Modifier.graphicsLayer {
              rotationZ = -rotation
              alpha = iconAlpha
            }
          )
        }
      }
    }
  ) { paddingValues ->
    val topPadding = paddingValues.calculateTopPadding()
    val topSpacerHeight = LocalWindowInfo.current.containerDpSize.height.div(9)
    val subtitleHeight = 24.dp
    val titleHeight = 64.dp
    val dividerPadding = 4.dp
    val dividerThickness = 1.dp

    val expandedHeight =
      topSpacerHeight + titleHeight + subtitleHeight + dividerPadding + dividerThickness
    val collapsedHeight = titleHeight + dividerThickness

    Box(
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
          alpha = if (isExiting) 1f - (exitScale.value - 1f) / 5f else 1f
        }
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = paddingValues.calculateBottomPadding())
          .graphicsLayer { clip = true }
          .drawWithContent {
            val currentHeaderHeight = with(density) {
              (expandedHeight + topPadding - (expandedHeight - collapsedHeight) * scrollProgress).toPx()
            }
            clipRect(top = currentHeaderHeight) {
              this@drawWithContent.drawContent()
            }
          }
      ) {
        item {
          Spacer(modifier = Modifier.height(expandedHeight + topPadding))
        }
        item {
          if (favoriteApps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Selected".uppercase(),
              style = MaterialTheme.typography.labelLarge,
              fontFamily = archivoBlackFamily,
              modifier = Modifier
                .animateItem()
                .padding(horizontal = 46.dp)
            )
          }
        }
        items(
          items = favoriteApps,
          key = { it.app.packageName }
        ) { appInfo ->
          SelectableApp(
            modifier = Modifier
              .animateItem()
              .padding(horizontal = 46.dp),
            appInfo = appInfo
          ) { onEvent(HomeEvent.ToggleFavorite(appInfo.app)) }
        }

        if (suggestedApps.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Suggestions".uppercase(),
              style = MaterialTheme.typography.labelLarge,
              fontFamily = archivoBlackFamily,
              modifier = Modifier
                .animateItem()
                .padding(horizontal = 46.dp)
            )
          }
          items(
            items = suggestedApps,
            key = { it.app.packageName }
          ) { appInfo ->
            SelectableApp(
              modifier = Modifier
                .animateItem()
                .padding(horizontal = 46.dp),
              appInfo = appInfo
            ) { onEvent(HomeEvent.ToggleFavorite(appInfo.app)) }
          }
        }

        if (otherApps.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = if (suggestedApps.isEmpty()) "Suggestions".uppercase() else "Other Apps".uppercase(),
              style = MaterialTheme.typography.labelLarge,
              fontFamily = archivoBlackFamily,
              modifier = Modifier
                .animateItem()
                .padding(horizontal = 46.dp)
            )
          }
          items(
            items = otherApps,
            key = { it.app.packageName }
          ) { appInfo ->
            SelectableApp(
              modifier = Modifier
                .animateItem()
                .padding(horizontal = 46.dp),
              appInfo = appInfo
            ) { onEvent(HomeEvent.ToggleFavorite(appInfo.app)) }
          }
        }
        item {
          Spacer(
            Modifier
              .navigationBarsPadding()
              .padding(bottom = 16.dp)
          )
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(expandedHeight + topPadding - (expandedHeight - collapsedHeight) * scrollProgress)
          .graphicsLayer { clip = true }
      ) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .graphicsLayer {
              alpha = scrollProgress
              scaleX = scrollProgress * 5f
              scaleY = scrollProgress * 5f
              rotationZ = scrollProgress * 45f
            }
            .background(
              color = MaterialTheme.colorScheme.surfaceContainerHigh,
              shape = MaterialShapes.Cookie6Sided.toShape()
            )
        )
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
        ) {
          Spacer(modifier = Modifier.height(topSpacerHeight * (1 - scrollProgress)))
          Text(
            text = "Favorites",
            style = MaterialTheme.typography.displayMedium,
            fontFamily = archivoBlackFamily,
            modifier = Modifier
              .height(titleHeight)
              .padding(horizontal = 46.dp)
              .wrapContentHeight(Alignment.CenterVertically)
          )
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .height(subtitleHeight * (1 - scrollProgress))
              .graphicsLayer {
                alpha = 1 - scrollProgress
                scaleY = 1 - scrollProgress
                transformOrigin = TransformOrigin(0.5f, 0f)
              }
          ) {
            Text(
              text = "Select your favorite apps to get started.",
              style = MaterialTheme.typography.bodyLarge,
              fontFamily = archivoFamily,
              modifier = Modifier.padding(horizontal = 46.dp)
            )
          }
          HorizontalDivider(
            modifier = Modifier
              .padding(top = dividerPadding * (1 - scrollProgress))
              .padding(horizontal = (46 * (1 - scrollProgress)).dp),
            color = MaterialTheme.colorScheme.onBackground
          )
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
      style = MaterialTheme.typography.bodyLargeEmphasized,
      fontFamily = archivoFamily,
      modifier = Modifier.padding(start = 16.dp)
    )
  }
}