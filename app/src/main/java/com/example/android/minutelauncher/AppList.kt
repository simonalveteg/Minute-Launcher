package com.example.android.minutelauncher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android.minutelauncher.db.App
import timber.log.Timber

@Composable
fun AppList(
  viewModel: LauncherViewModel = hiltViewModel(),
  focusRequester: FocusRequester = remember { FocusRequester() },
  onAppPress: (App) -> Unit = {},
  onBackPressed: () -> Unit = {}
) {
  val apps by viewModel.filteredApps.collectAsState(emptyList())
  val searchText by viewModel.searchTerm.collectAsState()

  BackHandler(true) {
    Timber.d("back pressed")
    onBackPressed()
  }

  Scaffold(floatingActionButton = {
    FloatingActionButton(onClick = {
      Timber.d("Search focused")
      focusRequester.requestFocus()
    }) {
      Icon(
        imageVector = Icons.Default.Search, contentDescription = "Search apps"
      )
    }
  }) { paddingValues ->
    Surface(modifier = Modifier.padding(paddingValues)) {
      Column {
        Row {
          TextField(
            value = searchText,
            onValueChange = { viewModel.onEvent(Event.UpdateSearch(it)) },
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester)
              .clearFocusOnKeyboardDismiss(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
              apps.firstOrNull()?.let {
                viewModel.onEvent(Event.OpenApplication(it))
              }
            }),
            colors = OutlinedTextFieldDefaults.colors(),
            placeholder = {
              Text(
                text = "search", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
              )
            },
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
          )
        }
        LazyColumn(
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
          item { Spacer(modifier = Modifier.height(24.dp)) }
          items(apps) { app ->
            Row {
              val appTitle = app.appTitle
              val appUsage by viewModel.getUsageForApp(app)

              AppCard(appTitle, appUsage) { onAppPress(app) }
            }
          }
        }
      }
    }
  }
}