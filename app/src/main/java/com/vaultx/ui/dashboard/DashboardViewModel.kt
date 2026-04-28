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

import com.vaultx.ui.audit.AuditResult
import com.vaultx.utils.SecurityAuditManager
import kotlinx.coroutines.flow.map

/**
 * ViewModel for the Dashboard screen.
 * Collects real-time password Flow and exposes filtered results as LiveData.
 */
class DashboardViewModel : ViewModel() {

    private val passwordRepository = AppModule.passwordRepository
    private val authRepository = AppModule.authRepository

    private val _passwords = MutableLiveData<Resource<List<PasswordEntry>>>()
    val passwords: LiveData<Resource<List<PasswordEntry>>> = _passwords

    private val _vaultHealth = MutableLiveData<AuditResult>()
    val vaultHealth: LiveData<AuditResult> = _vaultHealth

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
                        calculateHealth(allPasswords)
                        applyFilter()
                    }
                    is Resource.Error -> _passwords.value = resource
                    is Resource.Loading -> _passwords.value = resource
                }
            }
        }
    }

    private fun calculateHealth(entries: List<PasswordEntry>) {
        if (entries.isEmpty()) {
            _vaultHealth.value = AuditResult(100, emptyList(), emptyList(), emptyList())
            return
        }

        val weak = mutableListOf<PasswordEntry>()
        val reused = mutableListOf<PasswordEntry>()
        val healthy = mutableListOf<PasswordEntry>()

        val reusedMap = SecurityAuditManager.findReusedPasswords(entries) { 
            passwordRepository.decryptPassword(it) 
        }
        val allReusedIds = reusedMap.values.flatten().map { it.id }.toSet()

        entries.forEach { entry ->
            val plainText = passwordRepository.decryptPassword(entry.encryptedPassword)
            val strength = SecurityAuditManager.calculateStrength(plainText)
            
            val isWeak = strength == SecurityAuditManager.PasswordStrength.WEAK
            val isReused = allReusedIds.contains(entry.id)

            if (isWeak) weak.add(entry)
            if (isReused) reused.add(entry)
            if (!isWeak && !isReused) healthy.add(entry)
        }

        var score = 100
        val weakDeduction = (weak.size.toFloat() / entries.size * 50).toInt()
        val reusedDeduction = (reused.size.toFloat() / entries.size * 30).toInt()
        score = (100 - weakDeduction - reusedDeduction).coerceIn(0, 100)

        _vaultHealth.value = AuditResult(score, weak, reused, healthy)
    }

    /**
     * Filters passwords by title matching the search query.
     */
    fun searchPasswords(query: String) {
        currentQuery = query
        applyFilter()
    }

    /**
     * Toggles the favorite status of a password entry.
     */
    fun toggleFavorite(entry: PasswordEntry) {
        viewModelScope.launch {
            val updated = entry.copy(isFavorite = !entry.isFavorite)
            passwordRepository.savePassword(updated)
        }
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
        
        // Sort: Favorites first, then by title
        val sorted = filtered.sortedWith(
            compareByDescending<PasswordEntry> { it.isFavorite }
                .thenBy { it.title.lowercase() }
        )
        
        _passwords.value = Resource.Success(sorted)
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


