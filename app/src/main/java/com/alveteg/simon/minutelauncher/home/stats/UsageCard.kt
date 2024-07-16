package com.alveteg.simon.minutelauncher.home.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed

@Composable
fun UsageCard(
  label: String,
  usage: Long,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.large,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      AnimatedText(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontFamily = archivoFamily
      )
      AnimatedText(
        text = usage.toTimeUsed(false),
        style = MaterialTheme.typography.headlineSmall,
        fontFamily = archivoBlackFamily
      )
    }
  }
}


@Composable
fun AnimatedText(
  text: String,
  style: TextStyle,
  fontFamily: FontFamily
) {
  AnimatedContent(
    targetState = text,
    label = "Usage-text animation",
    transitionSpec = {
      (fadeIn()).togetherWith(fadeOut())
    }
  ) { string ->
    Text(text = string, style = style, fontFamily = fontFamily)
  }
}