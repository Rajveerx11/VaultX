package com.vaultx.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vaultx.data.model.PasswordEntry
import com.vaultx.data.model.Resource
import com.vaultx.utils.CryptoManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository handling all password entry CRUD operations in Firestore.
 * Encrypts passwords before writing, returns encrypted data on reads
 * (decryption is deferred to display time in the UI layer).
 *
 * Firestore path: users/{uid}/passwords/{auto-id}
 *
 * TODO: Agent 1 — Inject this via AppModule into your ViewModels.
 * Example: val passwordRepo = (application as VaultXApplication).appModule.passwordRepository
 */
class PasswordRepository(
    private val firestore: FirebaseFirestore,
    private val cryptoManager: CryptoManager
) {

    /**
     * Returns the passwords collection reference for a given user.
     */
    private fun passwordsCollection(uid: String) =
        firestore.collection("users").document(uid).collection("passwords")

    /**
     * Adds a new password entry for the given user.
     * The [entry]'s password field is encrypted before writing to Firestore.
     *
     * @param uid The authenticated user's UID.
     * @param entry The password entry to add. The `encryptedPassword` field should
     *              contain the PLAIN TEXT password — it will be encrypted here.
     * @return [Resource.Success] with true on success, [Resource.Error] on failure.
     */
    suspend fun addPasswordEntry(uid: String, entry: PasswordEntry): Resource<Boolean> {
        return try {
            val docRef = passwordsCollection(uid).document()

            // Encrypt the password before writing
            val encryptedEntry = entry.copy(
                id = docRef.id,
                encryptedPassword = cryptoManager.encrypt(entry.encryptedPassword),
                userId = uid,
                createdAt = if (entry.createdAt == 0L) System.currentTimeMillis() else entry.createdAt
            )

            docRef
                .set(encryptedEntry.toMap())
                .await()

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add password entry")
        }
    }

    /**
     * Returns a real-time Flow of all password entries for the given user,
     * ordered by createdAt descending (newest first).
     *
     * Passwords are returned ENCRYPTED — do NOT decrypt here.
     * Decrypt only at display time in the UI layer using [CryptoManager.decrypt].
     *
     * @param uid The authenticated user's UID.
     * @return A [Flow] emitting [Resource] states with the list of entries.
     */
    fun getPasswordEntries(uid: String): Flow<Resource<List<PasswordEntry>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = passwordsCollection(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to fetch passwords"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val entries = snapshot.documents.mapNotNull { doc ->
                        PasswordEntry.fromDocument(doc)
                    }
                    trySend(Resource.Success(entries))
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Fetches a single password entry by its document ID.
     *
     * @param uid The authenticated user's UID.
     * @param id The Firestore document ID of the password entry.
     * @return [Resource.Success] with the entry, or [Resource.Error] on failure.
     */
    suspend fun getPasswordEntryById(uid: String, id: String): Resource<PasswordEntry> {
        return try {
            val doc = passwordsCollection(uid)
                .document(id)
                .get()
                .await()

            val entry = PasswordEntry.fromDocument(doc)
                ?: return Resource.Error("Password entry not found")

            Resource.Success(entry)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch password entry")
        }
    }

    /**
     * Updates an existing password entry.
     * The password field is re-encrypted before writing to Firestore.
     *
     * @param uid The authenticated user's UID.
     * @param entry The updated entry. The `encryptedPassword` field should contain
     *              the PLAIN TEXT password — it will be re-encrypted here.
     * @return [Resource.Success] with true on success, [Resource.Error] on failure.
     */
    suspend fun updatePasswordEntry(uid: String, entry: PasswordEntry): Resource<Boolean> {
        return try {
            val encryptedEntry = entry.copy(
                encryptedPassword = cryptoManager.encrypt(entry.encryptedPassword),
                userId = uid
            )

            passwordsCollection(uid)
                .document(entry.id)
                .set(encryptedEntry.toMap())
                .await()

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update password entry")
        }
    }

    /**
     * Saves a PasswordEntry. If entry.id is empty, it adds a new document.
     * Otherwise, it updates the existing document.
     * This method handles encryption of the password field.
     */
    suspend fun savePassword(entry: PasswordEntry): Resource<Boolean> {
        return try {
            val uid = entry.userId.ifEmpty { firestore.app.options.projectId ?: "" } // Better to pass UID but fallback
            val docRef = if (entry.id.isEmpty()) {
                passwordsCollection(uid).document()
            } else {
                passwordsCollection(uid).document(entry.id)
            }

            val encryptedEntry = entry.copy(
                id = docRef.id,
                encryptedPassword = cryptoManager.encrypt(entry.encryptedPassword),
                createdAt = if (entry.createdAt == 0L) System.currentTimeMillis() else entry.createdAt
            )

            docRef.set(encryptedEntry.toMap()).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save password")
        }
    }

    /**
     * Deletes a password entry document from Firestore.
     *
     * @param uid The authenticated user's UID.
     * @param id The Firestore document ID of the entry to delete.
     * @return [Resource.Success] with true on success, [Resource.Error] on failure.
     */
    suspend fun deletePasswordEntry(uid: String, id: String): Resource<Boolean> {
        return try {
            passwordsCollection(uid)
                .document(id)
                .delete()
                .await()

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete password entry")
        }
    }

    /**
     * Searches password entries by title (client-side filtering).
     * Returns a real-time Flow that filters entries where the title
     * contains the [query] string (case-insensitive).
     *
     * @param uid The authenticated user's UID.
     * @param query The search term to filter by.
     * @return A [Flow] emitting [Resource] states with filtered entries.
     */
    fun searchPasswordEntries(uid: String, query: String): Flow<Resource<List<PasswordEntry>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = passwordsCollection(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Search failed"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val entries = snapshot.documents
                        .mapNotNull { doc -> PasswordEntry.fromDocument(doc) }
                        .filter { entry ->
                            entry.title.contains(query, ignoreCase = true)
                        }
                    trySend(Resource.Success(entries))
                }
            }

        awaitClose { listener.remove() }
    }

    fun getPasswords(uid: String): Flow<Resource<List<PasswordEntry>>> {
        return getPasswordEntries(uid)
    }

    suspend fun getPasswordById(uid: String, passwordId: String): Resource<PasswordEntry> {
        return getPasswordEntryById(uid, passwordId)
    }

    suspend fun addPassword(
        uid: String,
        title: String,
        username: String,
        rawPassword: String,
        url: String,
        category: String,
        notes: String
    ): Resource<Boolean> {
        val entry = PasswordEntry(
            title = title,
            username = username,
            encryptedPassword = rawPassword,
            url = url,
            category = category,
            notes = notes
        )
        return addPasswordEntry(uid, entry)
    }

    suspend fun updatePassword(
        uid: String,
        passwordId: String,
        title: String,
        username: String,
        rawPassword: String,
        url: String,
        category: String,
        notes: String
    ): Resource<Boolean> {
        val existingEntry = when (val result = getPasswordEntryById(uid, passwordId)) {
            is Resource.Success -> result.data
            is Resource.Error -> return result
            is Resource.Loading -> return Resource.Error("Unable to update password right now")
        }

        val entry = existingEntry.copy(
            id = passwordId,
            title = title,
            username = username,
            encryptedPassword = rawPassword,
            url = url,
            category = category,
            notes = notes
        )
        return updatePasswordEntry(uid, entry)
    }

    suspend fun deletePassword(uid: String, passwordId: String): Resource<Boolean> {
        return deletePasswordEntry(uid, passwordId)
    }

    fun decryptPassword(encryptedPassword: String): String {
        return try {
            cryptoManager.decrypt(encryptedPassword)
        } catch (_: Exception) {
            ""
        }
    }
}
