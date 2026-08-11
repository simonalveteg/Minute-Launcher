package com.alveteg.simon.minutelauncher.home.onboarding

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.home.HomeEvent
import com.alveteg.simon.minutelauncher.settings.PermissionCheckers
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SharedTransitionScope.PermissionsScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onFinish: () -> Unit,
    onEvent: (HomeEvent) -> Unit
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
                onFinish()
            }
            exitScale.animateTo(
                targetValue = 50f,
                animationSpec = tween(durationMillis = 600)
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
        floatingActionButton = {
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
                        scaleX = exitScale.value
                        scaleY = exitScale.value
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        alpha = iconAlpha
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 46.dp)
                .graphicsLayer {
                    alpha = if (isExiting) 1f - (exitScale.value - 1f) / 5f else 1f
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Permissions",
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = archivoBlackFamily,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To provide the best experience, we need a few permissions.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = archivoFamily,
                )
                Spacer(modifier = Modifier.height(32.dp))
                PermissionCheckers(
                    showDismissButton = false,
                    onEvent = onEvent
                )
            }
        }
    }
}
