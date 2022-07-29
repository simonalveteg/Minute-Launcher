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
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.android.minutelauncher.ui.theme.MinuteLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinuteLauncherTheme {
                if (!isAccessGranted(LocalContext.current)) {
                    // TODO: open dialog informing user about permission before opening settings
                    startActivity(Intent().apply { action = Settings.ACTION_USAGE_ACCESS_SETTINGS })
                }
                WindowCompat.setDecorFitsSystemWindows(window, false)
                AppList()
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