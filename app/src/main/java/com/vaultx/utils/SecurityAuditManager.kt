package com.vaultx.utils

import com.vaultx.data.model.PasswordEntry

/**
 * Utility for auditing password security.
 */
object SecurityAuditManager {

    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }

    /**
     * Calculates the strength of a password.
     */
    fun calculateStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK
        
        var score = 0
        if (password.length >= 12) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 4 -> PasswordStrength.VERY_STRONG
            score >= 3 -> PasswordStrength.STRONG
            score >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    /**
     * Finds reused passwords among entries.
     * Returns a map of password to list of entry IDs.
     */
    fun findReusedPasswords(entries: List<PasswordEntry>, decryptor: (String) -> String): Map<String, List<PasswordEntry>> {
        val decryptedMap = mutableMapOf<String, MutableList<PasswordEntry>>()
        entries.forEach { entry ->
            val plainText = decryptor(entry.encryptedPassword)
            if (plainText.isNotEmpty()) {
                decryptedMap.getOrPut(plainText) { mutableListOf() }.add(entry)
            }
        }
        return decryptedMap.filter { it.value.size > 1 }
    }
}
