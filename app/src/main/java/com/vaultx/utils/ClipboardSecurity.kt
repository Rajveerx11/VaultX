package com.vaultx.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper

object ClipboardSecurity {
    private const val CLEAR_DELAY_MS = 30_000L

    fun copySensitiveText(context: Context, label: String, text: String) {
        val appContext = context.applicationContext
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))

        Handler(Looper.getMainLooper()).postDelayed({
            clearIfUnchanged(appContext, label, text)
        }, CLEAR_DELAY_MS)
    }

    private fun clearIfUnchanged(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val currentClip = clipboard.primaryClip ?: return
        if (currentClip.itemCount == 0) return
        val currentLabel = currentClip.description.label?.toString()
        val currentText = currentClip.getItemAt(0)?.coerceToText(context)?.toString()

        if (currentLabel == label && currentText == text) {
            clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        }
    }
}
