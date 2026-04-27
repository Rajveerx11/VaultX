package com.vaultx.ui.generator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.security.SecureRandom

class GeneratorViewModel : ViewModel() {
    private val secureRandom = SecureRandom()

    private val _generatedPassword = MutableLiveData<String>()
    val generatedPassword: LiveData<String> = _generatedPassword

    private val _strength = MutableLiveData<String>()
    val strength: LiveData<String> = _strength

    var length = 16
    var useUpper = true
    var useLower = true
    var useNumbers = true
    var useSymbols = true

    init {
        generate()
    }

    fun generate() {
        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#\$%^&*_=+-/"

        var characterPool = ""
        if (useUpper) characterPool += uppercase
        if (useLower) characterPool += lowercase
        if (useNumbers) characterPool += numbers
        if (useSymbols) characterPool += symbols

        if (characterPool.isEmpty()) {
            _generatedPassword.value = ""
            _strength.value = "INVALID"
            return
        }

        val sb = java.lang.StringBuilder(length)
        // Ensure at least one character from each selected pool if length permits
        if (useUpper && length > sb.length) sb.append(secureRandomChar(uppercase))
        if (useLower && length > sb.length) sb.append(secureRandomChar(lowercase))
        if (useNumbers && length > sb.length) sb.append(secureRandomChar(numbers))
        if (useSymbols && length > sb.length) sb.append(secureRandomChar(symbols))

        val remainingLength = length - sb.length
        for (i in 0 until remainingLength) {
            sb.append(secureRandomChar(characterPool))
        }

        val result = sb.toString().toCharArray().apply { secureShuffle() }.concatToString()
        _generatedPassword.value = result

        calculateStrength(result)
    }

    private fun secureRandomChar(pool: String): Char {
        return pool[secureRandom.nextInt(pool.length)]
    }

    private fun CharArray.secureShuffle() {
        for (i in lastIndex downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val tmp = this[i]
            this[i] = this[j]
            this[j] = tmp
        }
    }

    private fun calculateStrength(password: String) {
        if (password.length < 8) {
            _strength.value = "WEAK"
        } else if (password.length >= 16 && useUpper && useNumbers && useSymbols) {
            _strength.value = "VERY STRONG"
        } else if (password.length >= 12 && useUpper && useNumbers) {
            _strength.value = "STRONG"
        } else {
            _strength.value = "MEDIUM"
        }
    }
}

