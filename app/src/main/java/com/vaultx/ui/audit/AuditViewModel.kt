package com.vaultx.ui.audit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultx.di.AppModule
import com.vaultx.data.model.PasswordEntry
import com.vaultx.data.model.Resource
import com.vaultx.utils.SecurityAuditManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuditResult(
    val score: Int,
    val weakPasswords: List<PasswordEntry>,
    val reusedPasswords: List<PasswordEntry>,
    val healthyPasswords: List<PasswordEntry>
)

class AuditViewModel : ViewModel() {

    private val passwordRepository = AppModule.passwordRepository
    private val authRepository = AppModule.authRepository

    private val _auditResult = MutableLiveData<Resource<AuditResult>>()
    val auditResult: LiveData<Resource<AuditResult>> = _auditResult

    fun runAudit() {
        val uid = authRepository.currentUser?.uid ?: return
        _auditResult.value = Resource.Loading()
        
        viewModelScope.launch {
            try {
                // Get one-shot list
                val entries = passwordRepository.getPasswords(uid).first()
                if (entries is Resource.Success) {
                    val result = calculateAudit(entries.data)
                    _auditResult.value = Resource.Success(result)
                } else if (entries is Resource.Error) {
                    _auditResult.value = Resource.Error(entries.message)
                }
            } catch (e: Exception) {
                _auditResult.value = Resource.Error(e.message ?: "Audit failed")
            }
        }
    }

    private fun calculateAudit(entries: List<PasswordEntry>): AuditResult {
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

        // Simple scoring: 100 - (deductions)
        var score = 100
        if (entries.isNotEmpty()) {
            val weakDeduction = (weak.size.toFloat() / entries.size * 50).toInt()
            val reusedDeduction = (reused.size.toFloat() / entries.size * 30).toInt()
            score = (100 - weakDeduction - reusedDeduction).coerceIn(0, 100)
        }

        return AuditResult(score, weak, reused, healthy)
    }
}
