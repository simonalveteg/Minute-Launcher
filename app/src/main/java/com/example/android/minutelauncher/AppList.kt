package com.example.android.minutelauncher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppList(
    listState: LazyListState,
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val mContext = LocalContext.current
    val installedPackages = viewModel.installedPackages

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(mContext, event.text, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.StartActivity -> {
                    mContext.startActivity(event.intent)
                }
            }
        }
    }

    Surface {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            }
            items(installedPackages) { app ->
                Row {
                    val appTitle by viewModel.getAppTitle(app)
                    val appUsage by viewModel.getUsageForApp(app.activityInfo.packageName)
                    AppCard(appTitle, appUsage) { viewModel.onEvent(Event.OpenApplication(app)) }
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