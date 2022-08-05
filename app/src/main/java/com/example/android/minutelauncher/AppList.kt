package com.example.android.minutelauncher

import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppList(
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val apps by viewModel.applicationList.collectAsState(emptyList())
  val searchText by viewModel.searchTerm
  val focusRequester = remember { FocusRequester() }

  BackHandler(true) {
    Log.d("NAV", "back pressed")
    viewModel.onEvent(Event.CloseAppsList)
  }

  LaunchedEffect(key1 = true) {
    viewModel.uiEvent.collect { event ->
      when (event) {
        is UiEvent.Search -> {
          Log.d("APP_LIST", "Search pressed")
          focusRequester.requestFocus()
        }
        else -> Unit
      }
    }
  }

  Scaffold(
    floatingActionButton = {
      LargeFloatingActionButton(
        onClick = { viewModel.onEvent(Event.SearchClicked) }
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
          contentDescription = "Search apps"
        )
      }
    }
  ) {
    Surface(modifier = Modifier.padding(it)) {
      Column {
        Row(
          Modifier
            .statusBarsPadding()
        ) {
          TextField(
            value = searchText,
            onValueChange = { viewModel.onEvent(Event.UpdateSearch(it)) },
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester)
              .clearFocusOnKeyboardDismiss(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
              viewModel.onEvent(Event.OpenApplication(apps.first()))
            }),
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            placeholder = {
              Text(
                text = "search",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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
          item {
            Spacer(modifier = Modifier.height(24.dp))
          }
          items(apps) { app ->
            Row {
              val appTitle = app.appTitle
              val appUsage by viewModel.getUsageForApp(app)
              AppCard(
                appTitle,
                appUsage,
                { viewModel.onEvent(Event.ShowAppInfo(app)) }
              ) { viewModel.onEvent(Event.OpenApplication(app)) }
            }
          }
        }
      }
    }
  }
}