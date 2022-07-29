package com.example.android.minutelauncher

sealed class Event {
    data class OpenApplication(val packageName: String): Event()
}
