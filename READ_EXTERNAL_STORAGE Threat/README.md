# 🔐 Android Security Demo: External Storage Threats & Controls

This repository showcases **two complementary Android apps** that demonstrate both a **real-world mobile security threat** and its **mitigation**.

> 📁 This repo contains **proof-of-concept apps** to simulate **data leakage** via external storage and how **encryption controls** can defend against it on Android devices.

---

## 🧭 What This Repo Demonstrates

| Aspect                     | Description                                                                 |
|---------------------------|-----------------------------------------------------------------------------|
| 🚨 **Threat**             | Apps with `READ_EXTERNAL_STORAGE` can silently harvest sensitive data.      |
| 🛡️ **Control**            | Secure logging using **AES encryption** and storage with **Android Keystore**.           |
| 🔍 **API Level Relevance**| Demonstrates behavior on **API 28 (Android 9.0)** where scoped storage is lax. |

---

## 📦 Included Apps

### 1. [`📱 Encrypted/Unencrypted Logger`](./mobile)

A **login-based app** that:
- Logs fake user credentials and timestamp to `/sdcard/Download/log.txt`.
- **Encrypts** logs using `crypto-js` before writing.
- Persists encryption key securely via **Android Keystore**.
- Simulates proper secure practices for external storage handling.

➡️ See [`mobile/README.md`](./mobile/README.md)

---

### 2. [`🍪 Cookie Clicker + Silent File Scanner`](./loggerMobile)

A **fun-looking cookie clicker app** that is actually:
- A **malicious scanner** that:
  - Requests `READ_EXTERNAL_STORAGE`.
  - Recursively scans for `.log` and `.txt` files in `/sdcard/`.
  - Displays and potentially exfiltrates them.
- Includes a **hidden button** that reveals the stolen data.

➡️ See [`loggerMobile/README.md`](./loggerMobile/README.md)

---

## 📂 Folder Structure

```
.
├── mobile/           # Unencrypted / Encrypted login + log writing demo
├── loggerMobile/     # Attacker app scanning external storage
└── README.md         # This overview
```

---