package com.vaultx.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper for biometric (fingerprint) authentication.
 * Uses PreferencesManager for toggle state — no raw SharedPreferences access.
 */
class BiometricHelper(
    private val preferencesManager: PreferencesManager
) {

    /**
     * Checks whether the device supports biometric authentication.
     */
    fun isBiometricAvailable(activity: FragmentActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Returns true if the user has enabled biometric lock in settings.
     */
    fun isBiometricEnabled(): Boolean = preferencesManager.isBiometricEnabled()

    /**
     * Shows the biometric prompt to the user.
     * @param activity The hosting FragmentActivity
     * @param onSuccess Called when authentication succeeds
     * @param onError Called when authentication fails or is cancelled
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't call onError here — the system will retry automatically
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("VaultX Authentication")
            .setSubtitle("Verify your identity to access the vault")
            .setNegativeButtonText("Cancel")
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}

