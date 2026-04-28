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

    fun getAutoLockTimeout(): Int {
        return preferencesManager.getAutoLockTimeout()
    }

    fun setAutoLockTimeout(minutes: Int) {
        preferencesManager.saveAutoLockTimeout(minutes)
    }

    fun logout() {
        authRepository.logoutUser()
        preferencesManager.clearAll()
    }

    suspend fun exportData(): String? {
        val userId = authRepository.getCurrentUserId() ?: return null
        val passwords = AppModule.passwordRepository.getAllPasswordsOnce(userId)
        
        val jsonArray = org.json.JSONArray()
        passwords.forEach { entry ->
            val obj = org.json.JSONObject()
            obj.put("title", entry.title)
            obj.put("username", entry.username)
            // Decrypt the password before exporting
            try {
                obj.put("password", AppModule.cryptoManager.decrypt(entry.encryptedPassword))
            } catch (e: Exception) {
                obj.put("password", "[Decryption Error]")
            }
            obj.put("url", entry.url)
            obj.put("category", entry.category)
            obj.put("notes", entry.notes)
            obj.put("isFavorite", entry.isFavorite)
            jsonArray.put(obj)
        }
        
        return jsonArray.toString(4)
    }
}

