package com.vaultx.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages app-level shared preferences for login state and biometric settings.
 *
 * TODO: Agent 1 — Access via AppModule:
 *   val prefs = (application as VaultXApplication).appModule.preferencesManager
 *   prefs.saveUserLoggedIn(true)
 */
class PreferencesManager(context: Context) {

    companion object {
        private const val PREF_NAME = "VaultX_Preferences"
        private const val KEY_USER_LOGGED_IN = "user_logged_in"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
        private const val KEY_LAST_ACTIVE_TIME = "last_active_time"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Saves whether the user is currently logged in.
     */
    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_USER_LOGGED_IN, isLoggedIn).apply()
    }

    /**
     * Returns true if the user is marked as logged in.
     */
    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_USER_LOGGED_IN, false)
    }

    fun saveUserProfile(name: String, email: String) {
        prefs.edit().putString(KEY_USER_NAME, name).putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    /**
     * Saves whether biometric authentication is enabled.
     */
    fun saveBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * Returns true if biometric authentication is enabled.
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Saves the auto-lock timeout in minutes.
     * 0 means immediate, -1 means never.
     */
    fun saveAutoLockTimeout(minutes: Int) {
        prefs.edit().putInt(KEY_AUTO_LOCK_TIMEOUT, minutes).apply()
    }

    fun getAutoLockTimeout(): Int {
        return prefs.getInt(KEY_AUTO_LOCK_TIMEOUT, 5) // Default to 5 minutes
    }

    fun saveLastActiveTime(timeMillis: Long) {
        prefs.edit().putLong(KEY_LAST_ACTIVE_TIME, timeMillis).apply()
    }

    fun getLastActiveTime(): Long {
        return prefs.getLong(KEY_LAST_ACTIVE_TIME, 0L)
    }

    /**
     * Clears all preferences. Call this on logout.
     *
     * TODO: Agent 1 — Call this from your logout flow in the ViewModel:
     *   preferencesManager.clearAll()
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
