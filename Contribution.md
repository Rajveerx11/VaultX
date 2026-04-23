# Contribution Guide

Thanks for your interest in contributing to VaultX.

This guide covers how to report issues, propose changes, and submit pull requests in a way that keeps the project secure and maintainable.

## Code of Conduct

- Be respectful and constructive.
- Focus on technical outcomes.
- Assume positive intent.

## Ways to Contribute

- Report bugs
- Improve documentation
- Add tests
- Improve UX/accessibility
- Fix security and stability issues
- Implement approved features

## Before You Start

1. Check existing issues/PRs to avoid duplicates.
2. For larger changes, open an issue first and discuss approach.
3. Keep PRs focused and scoped to one logical change.

## Local Development Setup

1. Fork and clone the repository.
2. Open the project in Android Studio.
3. Add your own Firebase config at `app/google-services.json` (never commit it).
4. Sync Gradle and verify build:
   - `./gradlew.bat :app:assembleDebug`

## Branching Convention

- Create a feature branch from `main`.
- Suggested branch names:
  - `feat/<short-description>`
  - `fix/<short-description>`
  - `docs/<short-description>`
  - `chore/<short-description>`

## Commit Message Style

Use clear, intent-based commits:

- `feat: add clipboard auto-clear timeout`
- `fix: prevent stale auth state after logout`
- `docs: update Firebase setup section`

Try to keep commits atomic and easy to review.

## Pull Request Requirements

Each PR should include:

- Problem statement and why the change is needed
- Summary of what changed
- Screenshots/video for UI changes (if applicable)
- Testing notes (what you ran and result)
- Security impact (if auth, crypto, storage, or rules were touched)

## Testing Expectations

Run applicable checks before opening a PR:

- `./gradlew.bat :app:assembleDebug`
- `./gradlew.bat test`
- `./gradlew.bat lint`

If a check cannot be run locally, mention it clearly in the PR.

## Security Contribution Rules (Important)

- Never commit real credentials, API keys, keystores, or tokens.
- Never commit `google-services.json`, `local.properties`, or signing config files.
- Do not log secrets, plaintext passwords, auth tokens, or full user payloads.
- Keep encryption/decryption logic changes small, reviewed, and tested.
- Any Firestore rule change must include explanation and validation examples.

## Sensitive Files To Keep Out of Git

- `app/google-services.json`
- `google-services.json`
- `local.properties`
- `*.jks`, `*.keystore`, `*.p12`
- `keystore.properties`
- any `.env*` containing secrets

## Style and Architecture Guidelines

- Kotlin-first, readable, and null-safe code.
- Prefer small functions and explicit naming.
- Keep business logic in repositories/ViewModels, not Fragments.
- Use existing `Resource` state pattern for async UI state.
- Avoid introducing new architecture patterns without discussion.

## Documentation Updates

If your change affects setup, architecture, or behavior, update relevant docs:

- `README.md`
- `FIREBASE_SETUP.md`
- `FIRESTORE_SCHEMA.md`
- this file (`Contribution.md`)

## How to Report Security Issues

If you discover a security vulnerability, do **not** open a public issue with exploit details.

Instead:

1. Open a minimal issue indicating a security concern exists, without sensitive details.
2. Ask maintainers for a private channel to share full reproduction and impact.

## Review Checklist (For Contributors)

- [ ] Build succeeds locally
- [ ] Tests/lint run or rationale provided
- [ ] No secrets or generated artifacts included
- [ ] Documentation updated when needed
- [ ] PR description clearly explains risk and rollout

Thanks for helping improve VaultX.
