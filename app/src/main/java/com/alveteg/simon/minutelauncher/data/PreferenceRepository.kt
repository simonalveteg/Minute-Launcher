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
  object Defaults {
    val APP_THEME = AppTheme.DARK
    const val USE_DYNAMIC_COLOR = true
    const val TRANSPARENCY_AMOUNT = 0.6f
    const val MINDFUL_DELAY_LENGTH = 5
    const val SHOW_DEFAULT_HOME_PROMPT = true
    const val SHOW_ADMIN_ACCESS_PROMPT = true
    const val SHOW_USAGE_ACCESS_PROMPT = true
    const val SKIP_APP_MODAL = false
    const val SHOW_ONBOARDING = true

    // Bounds for validation
    const val MIN_TRANSPARENCY = 0f
    const val MAX_TRANSPARENCY = 1f
    const val MIN_MINDFUL_DELAY = 0
    const val MAX_MINDFUL_DELAY = 30
  }

  private object PreferencesKeys {
    val APP_THEME = stringPreferencesKey("app_theme")
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    val TRANSPARENCY_AMOUNT = floatPreferencesKey("transparency_amount")
    val MINDFUL_DELAY_LENGTH = intPreferencesKey("timer_length") // Keeping "timer_length" key for migration
    val SHOW_DEFAULT_HOME_PROMPT = booleanPreferencesKey("show_default_home_prompt")
    val SHOW_ADMIN_ACCESS_PROMPT = booleanPreferencesKey("show_admin_access_prompt")
    val SHOW_USAGE_ACCESS_PROMPT = booleanPreferencesKey("show_usage_access_prompt")
    val SKIP_APP_MODAL = booleanPreferencesKey("skip_app_modal")
    val SHOW_ONBOARDING = booleanPreferencesKey("show_onboarding")
  }

  val appTheme: Flow<AppTheme> = context.dataStore.data
    .map { preferences ->
      val name = preferences[PreferencesKeys.APP_THEME] ?: Defaults.APP_THEME.name
      runCatching { AppTheme.valueOf(name) }.getOrDefault(Defaults.APP_THEME)
    }

  val useDynamicColor: Flow<Boolean> = context.dataStore.data
    .map { preferences -> preferences[PreferencesKeys.USE_DYNAMIC_COLOR] ?: Defaults.USE_DYNAMIC_COLOR }

  val transparencyAmount: Flow<Float> = context.dataStore.data
    .map { it[PreferencesKeys.TRANSPARENCY_AMOUNT] ?: Defaults.TRANSPARENCY_AMOUNT }

  val mindfulDelayLength: Flow<Int> = context.dataStore.data
    .map { it[PreferencesKeys.MINDFUL_DELAY_LENGTH] ?: Defaults.MINDFUL_DELAY_LENGTH }

  val showDefaultHomePrompt: Flow<Boolean> = context.dataStore.data
    .map { it[PreferencesKeys.SHOW_DEFAULT_HOME_PROMPT] ?: Defaults.SHOW_DEFAULT_HOME_PROMPT }

  val showAdminAccessPrompt: Flow<Boolean> = context.dataStore.data
    .map { it[PreferencesKeys.SHOW_ADMIN_ACCESS_PROMPT] ?: Defaults.SHOW_ADMIN_ACCESS_PROMPT }

  val showUsageAccessPrompt: Flow<Boolean> = context.dataStore.data
    .map { it[PreferencesKeys.SHOW_USAGE_ACCESS_PROMPT] ?: Defaults.SHOW_USAGE_ACCESS_PROMPT }

  val skipAppModal: Flow<Boolean> = context.dataStore.data
    .map { it[PreferencesKeys.SKIP_APP_MODAL] ?: Defaults.SKIP_APP_MODAL }

  val showOnboarding: Flow<Boolean> = context.dataStore.data
    .map { it[PreferencesKeys.SHOW_ONBOARDING] ?: Defaults.SHOW_ONBOARDING }

  suspend fun updateAppTheme(theme: AppTheme) {
    context.dataStore.edit { it[PreferencesKeys.APP_THEME] = theme.name }
  }

  suspend fun updateUseDynamicColor(enabled: Boolean) {
    context.dataStore.edit { it[PreferencesKeys.USE_DYNAMIC_COLOR] = enabled }
  }

  suspend fun updateTransparencyAmount(value: Float) {
    context.dataStore.edit {
      it[PreferencesKeys.TRANSPARENCY_AMOUNT] = value.coerceIn(Defaults.MIN_TRANSPARENCY, Defaults.MAX_TRANSPARENCY)
    }
  }

  suspend fun updateMindfulDelayLength(value: Int) {
    context.dataStore.edit {
      it[PreferencesKeys.MINDFUL_DELAY_LENGTH] = value.coerceIn(Defaults.MIN_MINDFUL_DELAY, Defaults.MAX_MINDFUL_DELAY)
    }
  }

  suspend fun updateSkipAppModal(skip: Boolean) {
    context.dataStore.edit { it[PreferencesKeys.SKIP_APP_MODAL] = skip }
  }

  suspend fun setShowDefaultHomePrompt(show: Boolean) {
    context.dataStore.edit { it[PreferencesKeys.SHOW_DEFAULT_HOME_PROMPT] = show }
  }

  suspend fun setShowAdminAccessPrompt(show: Boolean) {
    context.dataStore.edit { it[PreferencesKeys.SHOW_ADMIN_ACCESS_PROMPT] = show }
  }

  suspend fun setShowUsageAccessPrompt(show: Boolean) {
    context.dataStore.edit { it[PreferencesKeys.SHOW_USAGE_ACCESS_PROMPT] = show }
  }

  suspend fun updateShowOnboarding(show: Boolean) {
    context.dataStore.edit { it[PreferencesKeys.SHOW_ONBOARDING] = show }
  }
}