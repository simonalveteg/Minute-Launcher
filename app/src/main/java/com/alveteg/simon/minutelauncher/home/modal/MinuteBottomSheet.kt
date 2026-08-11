package com.alveteg.simon.minutelauncher.home.modal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily
import com.alveteg.simon.minutelauncher.theme.archivoFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinuteBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(),
  dragHandle: @Composable () -> Unit = {},
  title: String,
  description: String? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  ModalBottomSheet(
    sheetState = sheetState,
    onDismissRequest = onDismissRequest,
    dragHandle = dragHandle,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.headlineSmall,
      fontFamily = archivoBlackFamily,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
    )
    if (description != null) {
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        fontFamily = archivoFamily,
        modifier = Modifier.padding(horizontal = 32.dp).padding(top = 16.dp)
      )
    }
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(top = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      content()
      Spacer(
        modifier = Modifier.navigationBarsPadding().padding(bottom = 8.dp)
      )
    }
  }
}
