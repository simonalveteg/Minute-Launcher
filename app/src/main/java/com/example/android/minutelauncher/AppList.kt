package com.example.android.minutelauncher

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppList(
    listState: LazyListState,
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val apps = viewModel.applicationList
    val searchText = viewModel.searchTerm

    BackHandler(true) {
        Log.d("NAV","back pressed")
        viewModel.onEvent(Event.CloseAppsList)
    }

    Surface {
        Column {
            Row(
                Modifier
                    .statusBarsPadding()
            ) {
                TextField(
                    value = searchText.value,
                    onValueChange = { viewModel.onEvent(Event.UpdateSearch(it)) },
                    modifier = Modifier.fillMaxWidth().clearFocusOnKeyboardDismiss(),
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
                state = listState,
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
                        val appTitle by viewModel.getAppTitle(app)
                        val appUsage by viewModel.getUsageForApp(app.activityInfo.packageName)
                        AppCard(
                            appTitle,
                            appUsage
                        ) { viewModel.onEvent(Event.OpenApplication(app)) }
                    }
                }
            }
        }
    }
}

@Composable
fun AppCard(
    appTitle: String,
    appUsage: Long,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(2.dp)
            .clickable { onClick() }
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = appTitle,
            fontSize = 23.sp,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(appUsage.toTimeUsed(), color = MaterialTheme.colorScheme.primary)
    }
}