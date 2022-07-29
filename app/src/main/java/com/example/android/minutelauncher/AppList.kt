package com.example.android.minutelauncher

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {

        val mainIntent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pm = mContext.packageManager

        val installedPackages = pm.queryIntentActivities(mainIntent, 0).sortedBy {
            it.loadLabel(pm).toString().lowercase()
        }

        items(installedPackages) { app ->
            Row {
                val appTitle = app.loadLabel(pm).toString()
                AppCard(appTitle, viewModel.getUsageForApp(app.activityInfo.packageName)) {
                    Toast.makeText(mContext, appTitle, Toast.LENGTH_SHORT).show()
                    val intent = pm
                        .getLaunchIntentForPackage(app.activityInfo.packageName)
                        ?.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                        }
                    mContext.startActivity(intent)
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
        modifier = Modifier.padding(2.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = appTitle,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Clip,
            modifier = Modifier.clickable { onClick() }.fillMaxWidth()
        )
        Text("${appUsage.div(60000)} min")
    }
}