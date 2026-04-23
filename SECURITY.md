# Security Policy

## Supported Versions

VaultX is currently in early open-source development. Security updates are provided for the latest stable release line.

| Version | Supported |
| ------- | --------- |
| 1.x.x   | :white_check_mark: |
| < 1.0.0 | :x: |

## Reporting a Vulnerability

Please report security issues **privately**. Do not open a public GitHub issue for vulnerabilities.

### Preferred reporting method

1. Open a private GitHub Security Advisory (repository `Security` tab -> `Report a vulnerability`).
2. Include:
   - affected version/commit
   - impact summary
   - reproduction steps or PoC
   - suggested mitigation (if known)

### Response timeline

- Initial acknowledgement: **within 72 hours**
- Triage/update after validation: **within 7 days**
- Fix target (critical/high): **as soon as practical, typically within 14 days**

### What to expect

- If accepted, we will work on a fix and coordinate responsible disclosure.
- If declined, we will share a short rationale (for example: out-of-scope, non-reproducible, or expected behavior).
- We may request additional details to reproduce and validate impact.

## Scope

This policy covers security issues in:

- Android app code in this repository
- authentication and credential handling logic
- encryption/decryption flows
- Firestore rules and access-control logic

Out of scope:

- social engineering
- vulnerabilities in third-party services/providers outside this repo
- issues requiring rooted/jailbroken devices unless clearly security-relevant to normal users

## Disclosure Policy

Please allow time for a patch before public disclosure. After a fix is released, we may publish a security note/changelog entry describing affected versions and remediation guidance.
