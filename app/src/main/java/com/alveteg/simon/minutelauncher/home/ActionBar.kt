package com.alveteg.simon.minutelauncher.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.theme.archivoFamily

@Composable
fun ActionBar(
  actions: List<ActionBarAction>
) {
  val numberOfPriorityActions = 4
  var actionBarState by remember { mutableStateOf(ActionBarState.COLLAPSED) }
  val priorityActions = actions.take(numberOfPriorityActions)

  val showMoreIcon =
    if (actionBarState == ActionBarState.COLLAPSED) Icons.Default.ExpandMore else Icons.Default.ExpandLess

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    color = MaterialTheme.colorScheme.background,
    shape = MaterialTheme.shapes.large,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .animateContentSize()
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (actionBarState == ActionBarState.COLLAPSED) {
        Row(
          modifier = Modifier
            .height(60.dp)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          priorityActions.forEach {
            IconButton(
              onClick = it.action,
              enabled = it.enabled
            ) {
              Icon(imageVector = it.imageVector, contentDescription = it.description)
            }
          }
          IconButton(onClick = { actionBarState = actionBarState.toggle() }) {
            Icon(imageVector = showMoreIcon, contentDescription = "Show more actions")
          }
        }
      }
      if (actionBarState == ActionBarState.EXPANDED) {
        Spacer(modifier = Modifier.height(6.dp))
        actions.forEach {
          TextButton(
            enabled = it.enabled,
            onClick = it.action,
            colors = ButtonDefaults.textButtonColors(
              contentColor = LocalContentColor.current
            )
          ) {
            Icon(imageVector = it.imageVector, contentDescription = it.description)
            Text(
              text = it.description,
              fontFamily = archivoFamily,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Start,
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp)
            )
          }
        }
        IconButton(
          modifier = Modifier.fillMaxWidth(),
          onClick = { actionBarState = ActionBarState.COLLAPSED }
        ) {
          Icon(imageVector = showMoreIcon, contentDescription = "Show less actions")
        }
      }
    }
  }
}

data class ActionBarAction(
  val imageVector: ImageVector,
  val description: String,
  val action: () -> Unit,
  val enabled: Boolean = true
)

private enum class ActionBarState {
  COLLAPSED, EXPANDED;

  fun toggle(): ActionBarState {
    return when (this) {
      COLLAPSED -> EXPANDED
      EXPANDED -> COLLAPSED
    }
  }
}