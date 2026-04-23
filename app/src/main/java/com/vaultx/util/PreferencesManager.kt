package com.vaultx.util

import android.content.Context

/**
 * Centralized SharedPreferences manager for all app-level preferences.
 * All raw SharedPreferences access must go through this class.
 */
class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("vaultx_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    // --- Login State ---
    fun saveUserLoggedIn(isLoggedIn: Boolean) =
        prefs.edit().putBoolean(KEY_LOGGED_IN, isLoggedIn).apply()

    fun isUserLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    // --- Biometric ---
    fun saveBiometricEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    // --- User Profile Cache ---
    fun saveUserProfile(name: String, email: String) {
        prefs.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    // --- Clear All ---
    fun clearAll() = prefs.edit().clear().apply()
}

