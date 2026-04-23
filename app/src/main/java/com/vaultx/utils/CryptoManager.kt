package com.vaultx.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Handles AES-256-GCM encryption and decryption using the Android Keystore System.
 *
 * The encryption key is generated and stored securely inside the hardware-backed Keystore.
 * The key NEVER leaves the Keystore — all crypto operations are performed in-memory.
 *
 * Usage by UI layer:
 * ```
 * // TODO: Agent 1 — obtain CryptoManager from AppModule
 * val encrypted = cryptoManager.encrypt("myPassword123")
 * val decrypted = cryptoManager.decrypt(encrypted)
 * ```
 */
class CryptoManager {

    companion object {
        private const val KEY_ALIAS = "VaultX_MasterKey"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    init {
        // Generate the master key on first app launch if it doesn't already exist
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }

    /**
     * Generates a new AES-256 key and stores it in the Android Keystore.
     * The key is non-exportable — it can only be used for encryption/decryption
     * within the Keystore and never leaves the secure hardware.
     */
    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    /**
     * Retrieves the secret key from the Android Keystore.
     */
    private fun getSecretKey(): SecretKey {
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    /**
     * Encrypts the given [plainText] using AES-256-GCM.
     *
     * The IV (Initialization Vector) is generated automatically by the Cipher
     * and is prepended to the ciphertext before Base64 encoding.
     *
     * Format: Base64(IV + CipherText)
     *
     * @param plainText The raw password or text to encrypt.
     * @return A Base64-encoded string containing IV + ciphertext.
     * @throws Exception if encryption fails.
     */
    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv // 12 bytes, auto-generated
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Prepend IV to ciphertext for storage
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    /**
     * Decrypts the given [cipherText] that was encrypted with [encrypt].
     *
     * Extracts the IV from the first 12 bytes, then decrypts the remaining bytes.
     *
     * @param cipherText A Base64-encoded string containing IV + ciphertext.
     * @return The original plain text.
     * @throws Exception if decryption fails (e.g., tampered data, wrong key).
     */
    fun decrypt(cipherText: String): String {
        val combined = Base64.decode(cipherText, Base64.DEFAULT)

        // Extract IV (first 12 bytes) and ciphertext (remaining bytes)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
