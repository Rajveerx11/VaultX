package com.vaultx.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultx.di.AppModule
import com.vaultx.data.model.PasswordEntry
import com.vaultx.data.model.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen.
 * Collects real-time password Flow and exposes filtered results as LiveData.
 */
class DashboardViewModel : ViewModel() {

    private val passwordRepository = AppModule.passwordRepository
    private val authRepository = AppModule.authRepository

    private val _passwords = MutableLiveData<Resource<List<PasswordEntry>>>()
    val passwords: LiveData<Resource<List<PasswordEntry>>> = _passwords

    private var allPasswords: List<PasswordEntry> = emptyList()
    private var currentQuery: String = ""

    /**
     * Starts collecting the real-time password flow from Firestore.
     */
    fun loadPasswords() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            passwordRepository.getPasswords(uid).collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        allPasswords = resource.data
                        applyFilter()
                    }
                    is Resource.Error -> _passwords.value = resource
                    is Resource.Loading -> _passwords.value = resource
                }
            }
        }
    }

    /**
     * Filters passwords by title matching the search query.
     */
    fun searchPasswords(query: String) {
        currentQuery = query
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (currentQuery.isEmpty()) {
            allPasswords
        } else {
            allPasswords.filter {
                it.title.contains(currentQuery, ignoreCase = true) ||
                it.username.contains(currentQuery, ignoreCase = true)
            }
        }
        _passwords.value = Resource.Success(filtered)
    }

    /**
     * Decrypts a password for clipboard copy.
     */
    fun decryptPassword(encryptedPassword: String): String {
        return passwordRepository.decryptPassword(encryptedPassword)
    }

    fun getUserName(): String {
        return authRepository.currentUser?.displayName
            ?: AppModule.preferencesManager.getUserName()
    }

    fun logout() {
        authRepository.logoutUser()
        AppModule.preferencesManager.clearAll()
    }
}


