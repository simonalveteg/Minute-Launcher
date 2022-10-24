package com.example.android.minutelauncher

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

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

fun isAccessibilityServiceEnabled(
  context: Context,
  service: Class<out AccessibilityService?>
): Boolean {
  val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as (AccessibilityManager)
  val enabledServices: List<AccessibilityServiceInfo> =
    am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
  for (enabledService in enabledServices) {
    val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
    if (enabledServiceInfo.packageName.equals(context.packageName) && enabledServiceInfo.name.equals(
        service.name
      )
    ) return true
  }
  return false
}