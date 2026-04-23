package com.vaultx.ui.settings

import androidx.lifecycle.ViewModel
import com.vaultx.di.AppModule

class SettingsViewModel : ViewModel() {

    private val authRepository = AppModule.authRepository
    private val preferencesManager = AppModule.preferencesManager

    fun getUserName(): String {
        return AppModule.firebaseAuth.currentUser?.displayName.takeIf { !it.isNullOrBlank() } ?: preferencesManager.getUserName()
    }

    fun getUserEmail(): String {
        return AppModule.firebaseAuth.currentUser?.email ?: preferencesManager.getUserEmail()
    }

    fun isBiometricEnabled(): Boolean {
        return preferencesManager.isBiometricEnabled()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        preferencesManager.saveBiometricEnabled(enabled)
    }

    fun logout() {
        authRepository.logoutUser()
        preferencesManager.clearAll()
    }
}

