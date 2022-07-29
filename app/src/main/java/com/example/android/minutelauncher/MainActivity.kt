package com.example.android.minutelauncher

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.APP_OPS_SERVICE
import android.content.Intent
import android.content.Intent.ACTION_MAIN
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.android.minutelauncher.ui.theme.MinuteLauncherTheme
import java.util.*

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinuteLauncherTheme {
                val mContext = LocalContext.current
                if (!isAccessGranted(mContext)) {
                    // TODO: open dialog informing user about permission before opening settings
                    startActivity(Intent().apply { action = Settings.ACTION_USAGE_ACCESS_SETTINGS })
                }
                LaunchedEffect(key1 = 1) {
                    val currentTime = System.currentTimeMillis()
                    val startTime = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 5) }.timeInMillis
                    val usageStatsManager = mContext.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
                    val appList = usageStatsManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        startTime,
                        currentTime
                    )
                    appList.forEach {
                        Log.d("APP_USAGE","${it.packageName} used for: ${it.totalTimeInForeground}")
                    }
                }
                LazyColumn(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    listOfApps(mContext = mContext, goToApp = { startActivity(it) })
                }
            }
        }
    }
}

fun isAccessGranted(context: Context): Boolean {
    val appOpsManager = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
    return appOpsManager.unsafeCheckOpNoThrow(
        "android:get_usage_stats",
        android.os.Process.myUid(), context.packageName
    ) == AppOpsManager.MODE_ALLOWED

}

fun LazyListScope.listOfApps(
    mContext: Context,
    goToApp: (Intent?) -> Unit
) {
    val mainIntent = Intent().apply {
        action = ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val pm = mContext.packageManager

    val installedPackages = pm.queryIntentActivities(mainIntent, 0).sortedBy {
        it.loadLabel(pm).toString().lowercase()
    }

    items(installedPackages) { app ->
        Row {
            val appTitle = app.loadLabel(pm).toString()
            AppCard(appTitle) {
                Toast.makeText(mContext, appTitle, Toast.LENGTH_SHORT).show()
                val intent = pm
                    .getLaunchIntentForPackage(app.activityInfo.packageName)
                    ?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    }
                goToApp(intent)
            }
        }
    }
}

@Composable
fun AppCard(appTitle: String, onClick: () -> Unit) {
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
        Text("34 min")
    }
}