# VaultX

VaultX is an Android password manager focused on local-first encryption with cloud sync.  
Credentials are encrypted on-device using Android Keystore (AES-256-GCM) and stored in Cloud Firestore per authenticated user.

## Highlights

- Email/password authentication with Firebase Auth
- Google Sign-In support (via Credential/Google Play services setup)
- AES-256-GCM encryption using non-exportable Android Keystore keys
- Encrypted credential CRUD with Firestore security rules
- Password generator with configurable length and character sets
- Biometric app-lock support for returning sessions
- MVVM-style Android architecture using repositories and ViewModels

## Tech Stack

- **Language:** Kotlin
- **UI:** Fragments, ViewBinding, Material Components
- **Navigation:** AndroidX Navigation Component
- **Async:** Kotlin Coroutines + Flow
- **Backend:** Firebase Auth + Cloud Firestore
- **Security:** Android Keystore, AES/GCM
- **Build:** Gradle Kotlin DSL

## Project Structure

```text
app/src/main/java/com/vaultx/
  data/
    model/          # Resource, PasswordEntry, User, Category
    repository/     # AuthRepository, PasswordRepository
  di/               # AppModule
  ui/
    splash/
    auth/
    dashboard/
    addedit/
    detail/
    generator/
    settings/
  utils/            # CryptoManager, PreferencesManager, BiometricHelper
```

## Security Model

- Password plaintext is encrypted before upload to Firestore.
- Encryption uses AES-256-GCM with random IV per encryption.
- Secret key is created and held in Android Keystore and is non-exportable.
- Firestore rules restrict reads/writes to `users/{uid}` owner scope.
- Backup is disabled in Android manifest (`android:allowBackup="false"`) to reduce credential leakage through device/cloud backup channels.

## Prerequisites

- Android Studio (latest stable recommended)
- JDK 11
- Android SDK with `minSdk 26` and `compileSdk 36`
- Firebase project configured for Android package `com.vaultx`

## Setup

1. Clone the project.
2. Open in Android Studio.
3. Create `app/google-services.json` from your Firebase project and place it in `app/`.
4. Ensure Firebase Authentication (Email/Password and optionally Google) is enabled.
5. Create Firestore database and deploy project rules/indexes:
   - `firebase deploy --only firestore:rules`
   - `firebase deploy --only firestore:indexes`
6. Build and run:
   - `./gradlew.bat :app:assembleDebug` (Windows)

## Firebase Configuration Notes

- `google-services.json` is intentionally ignored by git and must not be committed.
- For Google Sign-In, make sure SHA-1 and SHA-256 fingerprints are registered for your Android app in Firebase.
- If `default_web_client_id` is missing, refresh and redownload `google-services.json` after enabling Google provider.

## Testing

- Unit tests:
  - `./gradlew.bat test`
- Instrumented tests:
  - `./gradlew.bat connectedAndroidTest`

Current test coverage is strongest around crypto behavior and basic template tests; contributions are welcome for repository and UI flows.

## Build and Quality Checks

- Build debug APK: `./gradlew.bat :app:assembleDebug`
- Run all lint checks: `./gradlew.bat lint`

## Open Source Readiness Checklist

- [x] Local environment files ignored (`local.properties`, `google-services.json`, keystore files)
- [x] Build artifacts and IDE noise ignored
- [x] Firestore security rules included
- [x] App backup disabled for sensitive data
- [ ] Add a `LICENSE` file before publishing (recommended)
- [ ] Add CI workflow for build/lint/test (recommended)

## Documentation

- `FIREBASE_SETUP.md` - Firebase integration notes
- `FIRESTORE_SCHEMA.md` - Firestore document model
- `firestore.rules` - Firestore authorization and validation rules

## Contributing

Please read `Contribution.md` before opening issues or pull requests.

## Disclaimer

VaultX is an educational/open-source project. Review, test, and harden thoroughly before using in high-risk production environments.
