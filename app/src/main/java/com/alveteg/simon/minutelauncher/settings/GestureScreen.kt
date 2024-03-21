package com.alveteg.simon.minutelauncher.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alveteg.simon.minutelauncher.data.LauncherViewModel
import com.alveteg.simon.minutelauncher.theme.archivoBlackFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureScreen(
  navController: NavController,
  viewModel: LauncherViewModel = hiltViewModel()
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Scaffold(
      topBar = {
        LargeTopAppBar(
          title = {
            Text(
              text = "Gesture Settings",
              fontFamily = archivoBlackFamily
            )
          },
          navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate Back"
              )
            }
          },
          scrollBehavior = scrollBehavior
        )
      }
    ) { paddingValues ->
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .nestedScroll(scrollBehavior.nestedScrollConnection)
          .padding(paddingValues)
          .padding(horizontal = 16.dp)
      ) {
        items(20) {
          GestureEntry(name = "Swipe left on upper half of screen")
        }
      }
    }
  }
}

@Composable
fun GestureEntry(
  name: String
) {
  Column {
    Text(text = name)
    Row {

    }
  }
}