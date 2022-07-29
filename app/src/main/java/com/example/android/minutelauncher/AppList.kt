package com.example.android.minutelauncher

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AppList(
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

    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        items(installedPackages) { app ->
            Row {
                val packageName = app.activityInfo.packageName
                val appTitle by viewModel.getAppTitle(app)
                val appUsage by viewModel.getUsageForApp(packageName)
                AppCard(appTitle, appUsage) { viewModel.onEvent(Event.OpenApplication(packageName)) }
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
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = appTitle,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .clickable { onClick() }
                .fillMaxWidth()
        )
        Text("${appUsage.div(60000)} min")
    }
}