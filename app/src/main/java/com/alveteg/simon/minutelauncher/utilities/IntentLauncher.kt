package com.alveteg.simon.minutelauncher.utilities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import timber.log.Timber


fun launchIntent(
  context: Context,
  intent: Intent,
  onNoActivityFound: (() -> Unit)? = null
) {
  Timber.d("Launching activity for intent: $intent")
  val modifiedIntent = intent.apply {
    flags += Intent.FLAG_ACTIVITY_NEW_TASK
  }
  try {
    ContextCompat.startActivity(context, modifiedIntent, null)
  } catch (e: ActivityNotFoundException) {
    Timber.d("-- couldn't launch activity.")
    onNoActivityFound?.invoke()
  }
}