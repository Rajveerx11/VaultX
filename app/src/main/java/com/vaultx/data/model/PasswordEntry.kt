package com.vaultx.data.model

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Represents a single password entry stored in Firestore
 * under "users/{uid}/passwords/{id}".
 *
 * The [encryptedPassword] field is always stored encrypted via [com.vaultx.utils.CryptoManager].
 * Decryption should ONLY happen at display time in the UI layer.
 */
data class PasswordEntry(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val username: String = "",
    val encryptedPassword: String = "",
    val url: String = "",
    val category: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = 0L
) {
    /**
     * Converts this PasswordEntry to a Map for Firestore document writes.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "userId" to userId,
        "title" to title,
        "username" to username,
        "encryptedPassword" to encryptedPassword,
        "url" to url,
        "category" to category,
        "notes" to notes,
        "isFavorite" to isFavorite,
        "createdAt" to createdAt
    )

    companion object {
        /**
         * Creates a PasswordEntry from a Firestore [DocumentSnapshot].
         * Returns null if parsing fails.
         */
        fun fromDocument(doc: DocumentSnapshot): PasswordEntry? {
            return try {
                PasswordEntry(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    title = doc.getString("title") ?: "",
                    username = doc.getString("username") ?: "",
                    encryptedPassword = doc.getString("encryptedPassword") ?: "",
                    url = doc.getString("url") ?: "",
                    category = doc.getString("category") ?: "",
                    notes = doc.getString("notes") ?: "",
                    isFavorite = doc.getBoolean("isFavorite") ?: false,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
