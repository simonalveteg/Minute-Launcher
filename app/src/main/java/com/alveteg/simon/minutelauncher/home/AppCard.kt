package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.theme.ScaleIndicationNodeFactory
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
  appInfo: AppInfo,
  onClick: () -> Unit
) {
  val appTitle = appInfo.app.appTitle
  val appUsage by remember { derivedStateOf { appInfo.usage.firstOrNull { it.usageDate == LocalDate.now() }?.usageDuration } }
  val interactionSource = remember { MutableInteractionSource() }

  Surface(
    tonalElevation = 2.dp,
    shape = MaterialTheme.shapes.large,
    color = Color.Transparent,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp, horizontal = 32.dp)
      .animateContentSize()
      .combinedClickable(
        indication = ScaleIndicationNodeFactory,
        interactionSource = interactionSource,
        onClick = onClick
      )
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(3.dp)
    ) {
      Text(
        text = appTitle,
        fontFamily = archivoFamily,
        fontSize = 25.sp,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Clip,
        modifier = Modifier
          .fillMaxWidth()
      )
      Text(
        text = appUsage.toTimeUsed(),
        fontFamily = archivoFamily,
        color = MaterialTheme.colorScheme.primary,
      )
    }
  }
}
