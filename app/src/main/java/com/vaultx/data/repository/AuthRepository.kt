package com.vaultx.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.vaultx.data.model.Resource
import com.vaultx.data.model.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Repository handling all Firebase Authentication operations.
 * Returns [Resource] for all operations so the UI layer can handle states uniformly.
 *
 * TODO: Agent 1 — Inject this via AppModule into your ViewModels.
 * Example: val authRepo = (application as VaultXApplication).appModule.authRepository
 */
class AuthRepository(
    private val auth: FirebaseAuth
) {
    companion object {
        private const val AUTH_TIMEOUT_MS = 15_000L
        private const val FIRESTORE_TIMEOUT_MS = 10_000L
        private const val TAG = "VaultXGoogleAuth"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isLoggedIn: Boolean
        get() = isUserLoggedIn()

    /**
     * Registers a new user with email/password, then creates a Firestore user document.
     *
     * Firestore path: users/{uid}
     *
     * @return [Resource.Success] with [User] on success, [Resource.Error] on failure.
     */
    suspend fun registerUser(name: String, email: String, password: String): Resource<User> {
        return try {
            val result = withTimeout(AUTH_TIMEOUT_MS) {
                auth.createUserWithEmailAndPassword(email, password).await()
            }
            val firebaseUser = result.user
                ?: return Resource.Error("Registration failed: user is null")

            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                createdAt = System.currentTimeMillis()
            )

            // Store user profile document in Firestore
            withTimeout(FIRESTORE_TIMEOUT_MS) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(firebaseUser.uid)
                    .set(user.toMap())
                    .await()
            }

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    /**
     * Signs in an existing user with email/password and fetches their Firestore profile.
     *
     * @return [Resource.Success] with [User] on success, [Resource.Error] on failure.
     */
    suspend fun loginUser(email: String, password: String): Resource<User> {
        return try {
            val result = withTimeout(AUTH_TIMEOUT_MS) {
                auth.signInWithEmailAndPassword(email, password).await()
            }
            val firebaseUser = result.user
                ?: return Resource.Error("Login failed: user is null")

            // Fetch user profile from Firestore
            val firestore = FirebaseFirestore.getInstance()
            val doc = withTimeout(FIRESTORE_TIMEOUT_MS) {
                firestore
                    .collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()
            }

            val user = if (doc.exists()) {
                User.fromMap(doc.data, firebaseUser.uid)
                    ?: return Resource.Error("Failed to parse user profile")
            } else {
                val recoveredUser = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: email,
                    createdAt = System.currentTimeMillis()
                )
                withTimeout(FIRESTORE_TIMEOUT_MS) {
                    firestore
                        .collection("users")
                        .document(firebaseUser.uid)
                        .set(recoveredUser.toMap())
                        .await()
                }
                recoveredUser
            }

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    /**
     * Handles Google Sign-In by exchanging the [idToken] for Firebase credentials.
     * Creates a new Firestore user document if this is the user's first login.
     *
     * @param idToken The Google ID token obtained from Google Sign-In.
     * @return [Resource.Success] with [User] on success, [Resource.Error] on failure.
     */
    suspend fun loginWithGoogle(idToken: String): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = withTimeout(AUTH_TIMEOUT_MS) {
                auth.signInWithCredential(credential).await()
            }
            val firebaseUser = result.user
                ?: return Resource.Error("Google sign-in failed: user is null")

            val firestore = FirebaseFirestore.getInstance()
            val userDoc = withTimeout(FIRESTORE_TIMEOUT_MS) {
                firestore
                    .collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()
            }

            val user: User
            if (userDoc.exists()) {
                // Existing user — fetch profile
                user = User.fromMap(userDoc.data, firebaseUser.uid)
                    ?: return Resource.Error("Failed to parse user profile")
            } else {
                // First-time Google login — create user document
                user = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    createdAt = System.currentTimeMillis()
                )
                withTimeout(FIRESTORE_TIMEOUT_MS) {
                    firestore
                        .collection("users")
                        .document(firebaseUser.uid)
                        .set(user.toMap())
                        .await()
                }
            }

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google sign-in failed")
        }
    }

    /**
     * Signs out the current user from Firebase Auth.
     *
     * TODO: Agent 1 — Also call PreferencesManager.clearAll() from your ViewModel on logout.
     */
    fun logoutUser() {
        auth.signOut()
    }

    /**
     * Returns the currently logged-in user's profile, or null if not logged in.
     * Fetches fresh data from Firestore each time.
     *
     * @return [User] or null.
     */
    suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = auth.currentUser ?: return null
            val firestore = FirebaseFirestore.getInstance()
            val doc = firestore
                .collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            if (doc.exists()) {
                User.fromMap(doc.data, firebaseUser.uid)
            } else {
                val recoveredUser = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    createdAt = System.currentTimeMillis()
                )
                firestore
                    .collection("users")
                    .document(firebaseUser.uid)
                    .set(recoveredUser.toMap())
                    .await()
                recoveredUser
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks whether a user is currently signed in.
     *
     * @return true if a user is authenticated, false otherwise.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun login(email: String, password: String): Resource<User> {
        return loginUser(email, password)
    }

    suspend fun register(name: String, email: String, password: String): Resource<User> {
        return registerUser(name, email, password)
    }

    fun logout() {
        logoutUser()
    }
}
