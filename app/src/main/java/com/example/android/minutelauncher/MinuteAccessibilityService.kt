package com.example.android.minutelauncher

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class MinuteAccessibilityService: AccessibilityService() {

  companion object {
    var a: MinuteAccessibilityService? = null

    fun turnScreenOff() {
      a?.performGlobalAction(8)
    }
  }

  override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
  }

  override fun onInterrupt() {
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    a = this
  }

  override fun onUnbind(intent: Intent?): Boolean {
    a = null
    return super.onUnbind(intent)
  }
}