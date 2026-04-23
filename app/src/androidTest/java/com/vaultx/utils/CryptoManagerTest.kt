package com.vaultx.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for [CryptoManager].
 *
 * These tests run on an Android device/emulator because the Android Keystore
 * is not available in JVM-only unit tests.
 */
@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    private lateinit var cryptoManager: CryptoManager

    @Before
    fun setUp() {
        cryptoManager = CryptoManager()
    }

    @Test
    fun encrypt_decrypt_roundTrip_returnsOriginalText() {
        val plainText = "MySecureP@ssw0rd!123"
        val encrypted = cryptoManager.encrypt(plainText)
        val decrypted = cryptoManager.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun encrypt_producesNonEmptyOutput() {
        val plainText = "TestPassword"
        val encrypted = cryptoManager.encrypt(plainText)

        assertNotNull(encrypted)
        assertTrue(encrypted.isNotEmpty())
    }

    @Test
    fun encrypt_outputDiffersFromPlainText() {
        val plainText = "AnotherPassword"
        val encrypted = cryptoManager.encrypt(plainText)

        assertNotEquals(plainText, encrypted)
    }

    @Test
    fun encrypt_sameInput_producesDifferentCipherTexts() {
        val plainText = "RepeatedPassword"
        val encrypted1 = cryptoManager.encrypt(plainText)
        val encrypted2 = cryptoManager.encrypt(plainText)

        assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    fun decrypt_afterMultipleEncryptions_allReturnOriginal() {
        val plainText = "MultiRoundTrip"
        val encrypted1 = cryptoManager.encrypt(plainText)
        val encrypted2 = cryptoManager.encrypt(plainText)

        assertEquals(plainText, cryptoManager.decrypt(encrypted1))
        assertEquals(plainText, cryptoManager.decrypt(encrypted2))
    }

    @Test
    fun encrypt_decrypt_emptyString_works() {
        val plainText = ""
        val encrypted = cryptoManager.encrypt(plainText)
        val decrypted = cryptoManager.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun encrypt_decrypt_specialCharacters_works() {
        val plainText = "p@\$\$w0rd!#%^&*()_+-=[]{}|;':\",./<>?"
        val encrypted = cryptoManager.encrypt(plainText)
        val decrypted = cryptoManager.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun encrypt_decrypt_mixedText_works() {
        val plainText = "VaultX-Password-Lock_2026"
        val encrypted = cryptoManager.encrypt(plainText)
        val decrypted = cryptoManager.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun encrypt_decrypt_longText_works() {
        val plainText = "A".repeat(10_000)
        val encrypted = cryptoManager.encrypt(plainText)
        val decrypted = cryptoManager.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }
}
