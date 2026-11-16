package com.alveteg.simon.minutelauncher.data

internal class UsageSessionCalculator(private var screenInteractive: Boolean) {
  private data class Session(
    val activities: MutableSet<String> = mutableSetOf(),
    var startedAt: Long? = null,
    var totalTime: Long = 0
  )

  private val sessions = mutableMapOf<String, Session>()

  fun resume(packageName: String, className: String?, time: Long) {
    if (!screenInteractive || className == null) return
    val session = sessions.getOrPut(packageName) { Session() }
    if (session.activities.add(className) && session.startedAt == null) {
      session.startedAt = time
    }
  }

  fun pause(packageName: String, className: String?, time: Long) {
    val session = sessions[packageName] ?: return
    if (session.activities.remove(className) && session.activities.isEmpty()) {
      close(session, time)
    }
  }

  fun screenInteractive() {
    screenInteractive = true
  }

  fun screenNonInteractive(time: Long) {
    closeAll(time)
    screenInteractive = false
  }

  fun shutdown(time: Long) {
    closeAll(time)
    screenInteractive = false
  }

  fun startup() {
    sessions.values.forEach {
      it.activities.clear()
      it.startedAt = null
    }
    screenInteractive = false
  }

  fun totals(): Map<String, Long> = sessions.mapValues { it.value.totalTime }

  private fun closeAll(time: Long) {
    sessions.values.forEach {
      close(it, time)
      it.activities.clear()
    }
  }

  private fun close(session: Session, time: Long) {
    session.startedAt?.let { session.totalTime += (time - it).coerceAtLeast(0) }
    session.startedAt = null
  }
}
