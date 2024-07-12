package com.alveteg.simon.minutelauncher.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private class ScaleIndicationNode(
  private val interactionSource: InteractionSource
) : Modifier.Node(), DrawModifierNode {
  var currentPressPosition: Offset = Offset.Zero
  val animatedScalePercent = Animatable(1f)

  var pressedAnimation: Job? = null
  var restingAnimation: Job? = null

  private suspend fun animateToPressed(pressPosition: Offset) {
    restingAnimation?.cancel()
    pressedAnimation?.cancel()
    pressedAnimation = coroutineScope.launch {
      currentPressPosition = pressPosition
      animatedScalePercent.snapTo(1f)
      animatedScalePercent.animateTo(0.9f, spring())
    }
  }

  private suspend fun animateToResting() {
    restingAnimation = coroutineScope.launch {
      // Wait for the existing press animation to finish if it is still ongoing
      pressedAnimation?.join()
      animatedScalePercent.animateTo(1f, tween(250))
    }
  }

  override fun onAttach() {
    coroutineScope.launch {
      interactionSource.interactions.collectLatest { interaction ->
        when (interaction) {
          is PressInteraction.Press -> animateToPressed(interaction.pressPosition)
          is PressInteraction.Release -> animateToResting()
          is PressInteraction.Cancel -> animateToResting()
        }
      }
    }
  }

  override fun ContentDrawScope.draw() {
    scale(
      scale = animatedScalePercent.value,
      pivot = currentPressPosition
    ) {
      this@draw.drawContent()
    }
  }
}

object ScaleIndicationNodeFactory : IndicationNodeFactory {
  override fun create(interactionSource: InteractionSource): DelegatableNode {
    return ScaleIndicationNode(interactionSource)
  }

  override fun hashCode(): Int = -1

  override fun equals(other: Any?) = other === this
}