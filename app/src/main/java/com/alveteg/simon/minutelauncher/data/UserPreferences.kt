package com.alveteg.simon.minutelauncher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alveteg.simon.minutelauncher.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")


@Singleton
class PreferenceRepository @Inject constructor(
  @ApplicationContext private val context: Context
) {
  private object PreferencesKeys {
    val APP_THEME = stringPreferencesKey("app_theme")
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    val TRANSPARENCY_AMOUNT = floatPreferencesKey("transparency_amount")
    val TIMER_LENGTH = intPreferencesKey("timer_length")
  }

  val appTheme: Flow<AppTheme> = context.dataStore.data
    .map { preferences ->
      val name = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.DARK.name
      runCatching { AppTheme.valueOf(name) }.getOrDefault(AppTheme.DARK)
    }

  val useDynamicColor: Flow<Boolean> = context.dataStore.data
    .map { preferences -> preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: true }

  val transparencyAmount: Flow<Float> = context.dataStore.data
    .map { it[PreferencesKeys.TRANSPARENCY_AMOUNT] ?: 0.5f }

  val timerLength: Flow<Int> = context.dataStore.data
    .map { it[PreferencesKeys.TIMER_LENGTH] ?: 5 }

  suspend fun updateAppTheme(theme: AppTheme) {
    context.dataStore.edit { it[PreferencesKeys.APP_THEME] = theme.name }
  }

  suspend fun updateUseDynamicColor(enabled: Boolean) {
    context.dataStore.edit { it[PreferencesKeys.USE_DYNAMIC_COLOR] = enabled }
  }

  suspend fun updateTransparencyAmount(value: Float) {
    context.dataStore.edit { it[PreferencesKeys.TRANSPARENCY_AMOUNT] = value.coerceIn(0f, 1f) }
  }

  suspend fun updateTimerLength(value: Int) {
    context.dataStore.edit { it[PreferencesKeys.TIMER_LENGTH] = value.coerceIn(0, 30) }
  }
}