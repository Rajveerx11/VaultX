package com.vaultx.data.model

/**
 * Represents a VaultX user profile stored in Firestore under "users/{uid}".
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L
) {
    /**
     * Converts this User to a Map for Firestore document writes.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "createdAt" to createdAt
    )

    companion object {
        /**
         * Creates a User from a Firestore document snapshot.
         * Returns null if parsing fails.
         */
        fun fromMap(map: Map<String, Any?>?, uid: String): User? {
            return try {
                User(
                    uid = uid,
                    name = map?.get("name") as? String ?: "",
                    email = map?.get("email") as? String ?: "",
                    createdAt = map?.get("createdAt") as? Long ?: 0L
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
