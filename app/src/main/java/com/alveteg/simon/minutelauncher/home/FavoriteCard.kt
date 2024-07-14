package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.theme.ScaleIndicationNodeFactory
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteCard(
  appInfo: AppInfo,
  onClick: () -> Unit
) {
  val interactionSource = remember { MutableInteractionSource() }
  val appTitle = appInfo.app.appTitle
  val appUsage by remember(appInfo) {
    derivedStateOf { appInfo.usage.firstOrNull { it.usageDate == LocalDate.now() }?.usageDuration }
  }

  Surface(
    shape = MaterialTheme.shapes.large,
    color = Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize()
      .combinedClickable(
        indication = ScaleIndicationNodeFactory,
        interactionSource = interactionSource,
        onClick = onClick
      )
      .padding(vertical = 2.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = appTitle,
        fontFamily = archivoBlackFamily,
        fontSize = 25.sp,
        textAlign = TextAlign.Center,
        style = LocalTextStyle.current.copy(
          shadow = Shadow(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
            blurRadius = 12f
          )
        ),
        overflow = TextOverflow.Clip,
        modifier = Modifier
          .fillMaxWidth()
      )
      Text(
        text = appUsage.toTimeUsed(),
        fontFamily = archivoFamily,
        color = MaterialTheme.colorScheme.primary,
        style = LocalTextStyle.current.copy(
          shadow = Shadow(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
            blurRadius = 12f
          )
        )
      )
    }
  }
}
