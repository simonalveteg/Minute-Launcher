package com.example.android.minutelauncher

import android.content.pm.ResolveInfo

sealed class Event {
    data class OpenApplication(val app: ResolveInfo): Event()
    data class UpdateSearch(val searchTerm: String): Event()
}
