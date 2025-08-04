# 📱 Mobile App: Logging to External Storage (Encrypted)

## 🔒 Purpose

This mobile application simulates a real-world **security risk**: apps writing sensitive information (like login credentials) to external storage where **other apps can access it**.

The goal is to **demonstrate both the vulnerability** and a **secure practice** using **AES encryption with Android Keystore**.

---

## 🧪 Features

- **Simple Login Screen**:
  - Takes in a username and password .
  - Logs the login timestamp, username, and a fake token.

- **Log Writing to External Storage**:
  - All logs are written to:
    ```
    /sdcard/Download/log.txt
    ```
  - This path is **world-readable** on older Android versions (API < 29), making it a potential target.

- **AES Encryption Integration**:
  - Before writing logs, contents are encrypted using **AES** from the `crypto-js` library.
  - Encrypted logs are unreadable to other apps unless the encryption key is known.

- **Android Keystore Integration**:
  - A **random 32-character hexadecimal passphrase** is generated on first app startup. (Not by Android KeyStore, that has a different implementation)
  - This passphrase is then securely stored using **Android Keystore** and is used as the base for encryption.
  - The key remains persisted across app restarts but is **wiped upon uninstall**.

---

## 🧬 How the Encryption Works

1. **Key Generation**:
   - On startup (if no key exists), the app generates:
     ```js
     const key = [...Array(32)]
        .map(() => Math.floor(Math.random() * 16).toString(16))
        .join('');
     ```
   - This passphrase is stored securely using `react-native-keychain` which resolves to Android Keystore.

2. **Encrypting Logs**:
   - `crypto-js` uses this passphrase to:
     - Derive an AES key.
     - Generate a random `salt` and `IV`.
     - Encrypt the stringified log data.
   - The final encrypted string is stored in `log.txt`.

3. **Decryption**:
   - Only the app can decrypt logs, since it has access to the passphrase.

---

## ⚠️ Why This Matters

Many older or poorly developed apps may **store unencrypted logs or data in external storage**. Attackers can:

- Request `READ_EXTERNAL_STORAGE` permission.
- Scan directories like `/sdcard/Download/` or `/sdcard/Android/media/`.
- Exfiltrate sensitive data silently.

By encrypting logs **before writing them**, even if external access is granted, **the data remains secure**.

---

## 🛡️ Security Controls Demonstrated

| Control                     | Description                                                |
|----------------------------|------------------------------------------------------------|
| ✅ Secure Log Storage      | Encrypting all logs before writing to disk                |
| ✅ Key Management           | Keys stored in Android Keystore, not hardcoded            |
| ✅ Scoped Access            | Only app can decrypt logs                                 |
| ⚠️ Insecure Scenario Simulated | Raw log data would be exposed if not encrypted       |

---

## 🌎 Demonstration Environment

| Component                | Value                            |
|--------------------------|----------------------------------|
| **Android Version**     | Android 9.0 (API 28, "Pie")       |
| **Device Architecture** | `arm64-v8a`                       |
| **Emulator Used**       | Android Studio Emulator           |
| **Scoped Storage**      | Not yet enforced (pre-Android 10) |

> ✅ Demonstrated on a platform where shared external storage is still accessible across apps — highlighting the risk of leaking sensitive data if encryption is not used.

---

## 📂 Demo Files

You can find the relevant files in:

- `LoginScreen.js` – UI and logic for login and logging
- `CryptoHelper.js` – AES encryption helper using `crypto-js`