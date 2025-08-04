# 🍪 Cookie Clicker (with Hidden Threat) – Attacker Demo

## 🎯 Purpose

This project simulates a **malicious app disguised as a harmless Cookie Clicker game**.

It demonstrates how an app that requests `READ_EXTERNAL_STORAGE` permission can **silently harvest sensitive files** from other apps that store logs or data in external storage.

---

## 🧪 Demonstration Environment

| Component                | Value                            |
|--------------------------|----------------------------------|
| **Android Version**     | Android 9.0 (API 28, "Pie")       |
| **Device Architecture** | `arm64-v8a`                       |
| **Emulator Used**       | Android Studio Emulator           |
| **Scoped Storage**      | Not yet enforced (pre-Android 10) |

> ✅ This version of Android still allows apps with `READ_EXTERNAL_STORAGE` to access shared `/sdcard/` files created by **other apps**, making it an ideal demonstration of the vulnerability.

---

## 🔍 Threat Scenario

This app works in tandem with the [Encrypted Logger App](https://github.com/nictjh/threatDemos/tree/master/READ_EXTERNAL_STORAGE%20Threat/mobile). While that app writes to external storage, this app shows how a malicious one can exploit it:

| Behavior                          | Description                                                                 |
|----------------------------------|-----------------------------------------------------------------------------|
| 🔓 **Permission Abuse**          | App requests `READ_EXTERNAL_STORAGE` for "saving cookie counts"            |
| 🕵️‍♂️ **Silent File Scanning**    | Recursively scans `/sdcard/` for all `.txt` and `.log` files               |
| 📤 **Data Exposure**             | Extracts contents and displays them in-app (can be sent to attacker server)|
| 🧠 **Deceptive UX**              | Appears to be a normal cookie clicker game                                 |
| 🔘 **Hidden Feature**            | Long-press or secret tap reveals internal logs of harvested data           |
| 🔁 **Periodic Scanning**         | Every 5 seconds, rescans for new or changed files                          |

---

## 🧪 Features

- **Cookie Clicker UI**:
  - Incremental tap-based game to make the app appear legitimate.
  - Fake feature: "Saving cookie score to file" requires storage permission.

- **Recursive Scanner**:
  - Searches every subdirectory under `/sdcard/`.
  - Filters only `.txt` and `.log` files.
  - Reads and logs file content (e.g., credentials, tokens, debug logs).

- **Hidden Debug Panel**:
  - Activated via secret gesture (e.g., long-press on app version).
  - Shows list of discovered files and their contents.

- **Periodic Polling**:
  - Rechecks `/sdcard/` every 5 seconds for new or modified files.

---

## 🚨 Why This is Dangerous

Apps with `READ_EXTERNAL_STORAGE` permission can **access any file stored by other apps** if those files are not protected properly.

### Combined with the Previous App:

- If the **first app writes unencrypted logs to external storage**, this app can steal them.
- Demonstrates the importance of:
  - **Avoiding sensitive writes to shared storage**.
  - **Encrypting external data**.
  - **Avoiding unnecessary permission grants**.

---

## 📂 Demo Structure

- `HomeScreen.js` – Main clicker logic
- `SecretScanner.js` – Debug view to display harvested file contents

---

## 🛡️ Controls Bypassed

| Threat                             | Realistic Impact                                        |
|-----------------------------------|----------------------------------------------------------|
| ❌ Insecure Storage by Other Apps | Able to harvest logs stored in shared storage           |
| ❌ User Trust                      | UI masks true behavior to deceive users                 |
| ❌ Permission Granularity         | Android APIs pre-29 treat `/sdcard/` as globally readable|

---

## ✅ What Prevents This?

If paired with the [Encrypted Logger App](https://github.com/nictjh/threatDemos/tree/master/READ_EXTERNAL_STORAGE%20Threat/mobile):

- All logs stored in `/sdcard/` are AES-encrypted.
- Attacker app can still read files — but can't decipher them.
- Demonstrates how **encryption** combined with **least privilege** principles protect user data.

---
