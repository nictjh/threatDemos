# 🧹 Mobile App: Cache Cleanup (Victim App)

## 🔒 Purpose

This app simulates a **"Device Booster" utility** that exposes a broadcast receiver to allow trusted apps to **remotely trigger cache cleanup** operations.

Originally intended for automation and internal tooling, this design **introduces a security risk** when not properly protected.

---

## 🧪 Features

- **Exposed Broadcast Receiver**:
  - `com.victimapp.MySensitiveReceiver` listens for cleanup intents.

- **Supported Payloads**:
  - `"LEGIT_CLEANUP"` – Simulates a real cache cleanup.

- **On receiving a broadcast**:
  - Vibrates the device.
  - Displays a persistent toast message.
  - Launches the app (if in background).

---

## 🧬 Technical Behavior

- The receiver accepts an intent with extras:
  - `payload`: `"LEGIT_CLEANUP"`
  - `caller_package`: Used for basic trust filtering.

- Vibrates the device:
  ```kotlin
  vibrator.vibrate(VibrationEffect.createOneShot(500, DEFAULT_AMPLITUDE))
  ```

- Displays a toast:
  ```kotlin
  Toast.makeText(context, message, Toast.LENGTH_LONG).show()
  ```

- Launches the app if backgrounded:
  ```kotlin
  context.packageManager.getLaunchIntentForPackage("com.victimapp")
  ```

---

## ⚠️ Why This Matters

This app demonstrates a **common Android vulnerability**: exposed broadcast receivers performing sensitive actions without sufficient protection.

Key issues:

- The app exports a broadcast receiver:

  ```xml
  com.victimapp.MySensitiveReceiver
  ```

- The receiver is "protected" only by a custom permission:

  ```xml
  <uses-permission android:name="com.victimapp.SENSITIVE_PERMISSION" />
  ```

- This permission can be easily discovered via tools like `apktool` or `jadx`.

- A malicious app can **declare the same permission** in its manifest to **bypass this protection** and send unauthorized broadcasts.

- There is **no signature-level enforcement**, meaning **any third-party app** can exploit the receiver.

> This simulates how internal functionality can become an **attack surface**, enabling **intent spoofing** and **privilege escalation**.

---

## 🔓 Current Security Posture *(Demo Setup)*

| Control                     | Status     | Description                                                                 |
|-----------------------------|------------|-----------------------------------------------------------------------------|
| Custom Permission Guard     | ⚠️ Weak     | Protected by `com.victimapp.SENSITIVE_PERMISSION`, but easily guessable via static analysis |
| Signature-Level Protection  | ❌ Missing | No `signature` set in permission declaration |         |                       |
| Exported Receiver           | ✅ Present  | Explicitly exported to demonstrate vulnerability                           |

---

## 🛡️ Possible Security Controls

To mitigate this vulnerability in a production app:

| Control                    | Recommendation                                                                 |
|----------------------------|---------------------------------------------------------------------------------|
| Signature-Level Permission | Use `protectionLevel="signature"` to restrict access to same-signed apps only  |
| Internal-Only Receivers    | Set `android:exported="false"` for components not meant to be public           |
| Caller Package Filtering    | Use `caller_package` to check within allowed Lists |


---

## 🌎 Demonstration Environment

| Component                | Value                            |
|--------------------------|----------------------------------|
| **Android Version**     | Android 9.0 (API 28, "Pie")       |
| **Device Architecture** | `arm64-v8a`                       |
| **Emulator Used**       | Android Studio Emulator           |
| **Scoped Storage**      | Not enforced (pre-Android 10)     |

> ✅ This app intentionally exposes risky behavior for educational purposes.
> It highlights how seemingly helpful features can become **attack vectors** if improperly secured.
