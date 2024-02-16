package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.utilities.Gesture

@Composable
fun ConstraintLayoutScope.GestureApps(
  screenState: ScreenState,
  screenHeight: Float,
  apps: Map<Gesture, App>,
  onClick: (Gesture) -> Unit,
  crTopLeft: ConstrainedLayoutReference,
  crTopRight: ConstrainedLayoutReference,
  crBottomLeft: ConstrainedLayoutReference,
  crBottomRight: ConstrainedLayoutReference,
) {
  val density = LocalDensity.current
  val sideWidth by animateFloatAsState(
    targetValue = if (screenState.isModify()) 60f else 0f,
    label = "",
    animationSpec = tween(200, 0, EaseInOutQuad)
  )
  val width = sideWidth.dp
  val height = with(density) { screenHeight.div(2).toDp() - 80.dp }
  val corner = 64.dp
  val leftShape = RoundedCornerShape(0.dp, corner, corner, 0.dp)
  val rightShape = RoundedCornerShape(corner, 0.dp, 0.dp, corner)

  GestureCard(
    app = apps[Gesture.UPPER_RIGHT],
    gesture = Gesture.UPPER_RIGHT,
    height = height,
    width = width,
    shape = leftShape,
    constraintReference = crTopLeft,
    constraints = {
      start.linkTo(parent.start)
      top.linkTo(parent.top)
    },
    onClick = onClick
  )

  GestureCard(
    app = apps[Gesture.UPPER_LEFT],
    gesture = Gesture.UPPER_LEFT,
    height = height,
    width = width,
    shape = rightShape,
    constraintReference = crTopRight,
    constraints = {
      end.linkTo(parent.end)
      top.linkTo(parent.top)
    },
    onClick = onClick
  )

  GestureCard(
    app = apps[Gesture.LOWER_RIGHT],
    gesture = Gesture.LOWER_RIGHT,
    height = height,
    width = width,
    shape = leftShape,
    constraintReference = crBottomLeft,
    constraints = {
      start.linkTo(parent.start)
      bottom.linkTo(parent.bottom)
    },
    onClick = onClick
  )

  GestureCard(
    app = apps[Gesture.LOWER_LEFT],
    gesture = Gesture.LOWER_LEFT,
    height = height,
    width = width,
    shape = rightShape,
    constraintReference = crBottomRight,
    constraints = {
      end.linkTo(parent.end)
      bottom.linkTo(parent.bottom)
    },
    onClick = onClick
  )
}


@Composable
fun ConstraintLayoutScope.GestureCard(
  app: App?,
  gesture: Gesture,
  height: Dp,
  width: Dp,
  shape: Shape,
  constraintReference: ConstrainedLayoutReference,
  constraints: ConstrainScope.() -> Unit,
  onClick: (Gesture) -> Unit
) {
  val title = app?.appTitle ?: ""
  Surface(modifier = Modifier
    .width(width)
    .height(height)
    .padding(vertical = 24.dp)
    .constrainAs(constraintReference) { constraints() },
    shape = shape,
    tonalElevation = 1.dp,
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    onClick = {
      onClick(gesture)
    }) {
    Column(
      verticalArrangement = Arrangement.Center,
    ) {
      title.forEach { char ->
        Text(
          text = char.toString(),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
