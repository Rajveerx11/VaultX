# Firebase Setup Required

VaultX already has the Firebase Gradle plugin, Firebase Auth, and Firestore wired into the app.
The remaining console-side setup is what determines whether Google Authentication works at runtime.

## Database used by this app

VaultX uses **Cloud Firestore**.

It does **not** currently store password data in **Firebase Realtime Database**.
If you are checking Realtime Database for tables, you will not see the app data there.

Open Firestore instead:

`Firebase Console -> Build -> Firestore Database -> Data`

See `FIRESTORE_SCHEMA.md` for the collection structure.

## What to verify locally

1. `app/google-services.json` exists locally and matches package name `com.vaultx`.
2. Firebase Auth and Firestore dependencies are included in the app module.
3. Firebase is initialized through the standard Android setup.
4. Firestore reads and writes target `users/{uid}` and `users/{uid}/passwords/{passwordId}`.

## Common blocker for Google Authentication

If `app/google-services.json` is outdated or generated before SHA fingerprints and Google provider setup,
the OAuth metadata required for Google sign-in may be missing and `default_web_client_id` will not resolve at runtime.

## Required Firebase console steps

1. Open the Firebase console for project `vaultx-c0d93`.
2. Open Project settings, then the Android app `com.vaultx`.
3. Add both the debug and release SHA-1 and SHA-256 fingerprints.
4. Open Authentication -> Sign-in method and enable `Google`.
5. Download the updated `google-services.json`.
6. Replace `app/google-services.json` with the newly downloaded file.
7. Rebuild the app.
8. Create Firestore if it is not already enabled.
9. Deploy rules with `firebase deploy --only firestore:rules`.
10. Deploy indexes with `firebase deploy --only firestore:indexes`.

## Important

- Keep `app/google-services.json` out of version control.
- If Google sign-in still shows as unavailable after updating the config file, confirm the package name is exactly `com.vaultx` and that the SHA fingerprints were added to the same Firebase app entry.
