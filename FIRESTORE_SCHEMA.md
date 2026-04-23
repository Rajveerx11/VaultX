# VaultX Firestore Schema

VaultX uses **Cloud Firestore**, not Firebase Realtime Database.

The app's data model is:

## `users`

Path:

`users/{uid}`

Fields:

- `uid: string`
- `name: string`
- `email: string`
- `createdAt: number`

## `users/{uid}/passwords`

Path:

`users/{uid}/passwords/{passwordId}`

Fields:

- `id: string`
- `userId: string`
- `title: string`
- `username: string`
- `encryptedPassword: string`
- `url: string`
- `category: string`
- `notes: string`
- `createdAt: number`

Allowed category values:

- `SOCIAL`
- `BANKING`
- `WORK`
- `OTHERS`

## When collections appear

Firestore does not pre-create empty tables/collections.

Collections appear automatically when the first documents are written:

- `users/{uid}` is created during sign-up, Google sign-in, or a recovered first login.
- `users/{uid}/passwords/{passwordId}` is created when the first password entry is saved.
