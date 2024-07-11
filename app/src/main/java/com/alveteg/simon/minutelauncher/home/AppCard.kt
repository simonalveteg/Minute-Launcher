package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alveteg.simon.minutelauncher.data.UsageStatistics
import com.alveteg.simon.minutelauncher.theme.archivoFamily
import com.alveteg.simon.minutelauncher.utilities.toTimeUsed
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
  appTitle: String,
  appUsage: List<UsageStatistics>,
  selected: Boolean = false,
  onLongClick: () -> Unit = {},
  onClick: () -> Unit
) {

  val surfaceColor by animateColorAsState(
    targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
    label = "",
    animationSpec = tween(700)
  )

  Surface(
    tonalElevation = 2.dp,
    shape = MaterialTheme.shapes.large,
    color = surfaceColor,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp, horizontal = 32.dp)
      .animateContentSize()
      .combinedClickable(
//        onLongClick = onLongClick,
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
        text = appUsage.firstOrNull{ it.usageDate == LocalDate.now() }?.usageDuration.toTimeUsed(),
        fontFamily = archivoFamily,
        color = MaterialTheme.colorScheme.primary,
      )
    }
  }
}
