package com.alveteg.simon.minutelauncher.home.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alveteg.simon.minutelauncher.Event
import com.alveteg.simon.minutelauncher.data.App
import com.alveteg.simon.minutelauncher.data.AppInfo
import com.alveteg.simon.minutelauncher.home.AppCard
import com.alveteg.simon.minutelauncher.home.ScreenState
import com.alveteg.simon.minutelauncher.home.modal.AppModalActionBar
import com.alveteg.simon.minutelauncher.home.stats.UsageBarGraph
import com.alveteg.simon.minutelauncher.home.stats.UsageCard
import com.alveteg.simon.minutelauncher.utilities.clearFocusOnKeyboardDismiss

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
  screenState: ScreenState,
  onEvent: (Event) -> Unit,
  searchText: String,
  apps: List<AppInfo>,
  offsetY: Animatable<Float, AnimationVector1D>,
  onAppClick: (AppInfo) -> Unit,
  onSearch: KeyboardActionScope.() -> Unit
) {
  AnimatedVisibility(
    visible = screenState.isDashboard(),
    enter = fadeIn(),
    exit = fadeOut()
  ) {
    val peekHeight = remember { Animatable(0f) }
    LaunchedEffect(true) {
      peekHeight.animateTo(100f)
    }
    DisposableEffect(Unit) {
      onDispose {
        onEvent(Event.UpdateSearch(""))
      }
    }
    var searchHeight by remember { mutableStateOf(0.dp) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val focusRequester = remember { FocusRequester() }
    val density = LocalDensity.current
    BottomSheetScaffold(
      scaffoldState = scaffoldState,
      sheetPeekHeight = peekHeight.value.dp,
      sheetDragHandle = {},
      sheetContent = {
        Column(
          modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp, top = 16.dp)
        ) {
          Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp,
            modifier = Modifier
              .onGloballyPositioned {
                searchHeight = with(density) { it.size.height.toDp() }
              }
              .navigationBarsPadding()
          ) {
            TextField(
              value = searchText,
              onValueChange = { onEvent(Event.UpdateSearch(it)) },
              modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .clearFocusOnKeyboardDismiss(),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
              keyboardActions = KeyboardActions(onSearch = onSearch),
              colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
              ),
              placeholder = {
                Text(
                  text = "search",
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth()
                )
              },
              textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )
          }
          Spacer(Modifier.height(8.dp))
          UsageBarGraph()
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp)
          ) {
            UsageCard(
              label = "7 day average",
              usage = 10000000L,
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            UsageCard(
              label = "Today",
              usage = 20000000L,
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            )
          }
          AppModalActionBar(
            appInfo = AppInfo(App(0, "", ""), true),
            enabled = true,
            onChangeTimer = { /*TODO*/ },
            onEvent = {}
          )
        }
      },
      containerColor = Color.Transparent
    ) {
      val listState = rememberLazyListState()
      LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.Bottom,
        reverseLayout = true,
        modifier = Modifier
          .fillMaxSize()
          .offset(y = offsetY.value.dp)
      ) {
        item {
          Spacer(
            modifier = Modifier
              .navigationBarsPadding()
              .height(searchHeight)
          )
        }
        items(items = apps) { appInfo ->
          val appTitle = appInfo.app.appTitle
          val appUsage = appInfo.usage
          AppCard(appTitle, appUsage) { onAppClick(appInfo) }
        }
        item {
          Spacer(modifier = Modifier.statusBarsPadding())
        }
      }
    }
  }
}

@Composable
fun asss() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
    ) {
      UsageCard(
        label = "7 day average",
        usage = 10000000L,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      )
      UsageCard(
        label = "Today",
        usage = 20000000L,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      )
    }
    UsageBarGraph()
    AppModalActionBar(
      appInfo = AppInfo(App(0, "", ""), true),
      enabled = true,
      onChangeTimer = { /*TODO*/ },
      onEvent = {}
    )
    Button(
      onClick = { }, modifier = Modifier
        .padding(48.dp)
        .padding(top = 280.dp)
    ) {
      Text(text = "Show Apps")
    }
  }
}