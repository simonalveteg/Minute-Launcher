package com.example.android.minutelauncher

fun Long.toTimeUsed(): String {
    val minutes = div(60000)
    val hours = minutes.div(60)
    val sb = StringBuilder()
    if (hours != 0L) sb.append("${hours}h ")
    if (minutes != 0L) sb.append("${minutes % 60}min")
    return sb.toString()
}