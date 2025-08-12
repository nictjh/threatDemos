# 🧪 Android Broadcast Security Demo

This project demonstrates a common Android security pitfall: **exported broadcast receivers** that can be abused due to **weak or missing access control**.

It consists of two apps:

---

## 📱 Victim App: Cache Cleaner

A simulated "Device Booster" utility that exposes a sensitive broadcast receiver.

### 🔹 Purpose

Originally designed to allow **trusted apps** to remotely trigger cache cleanup functionality — e.g., as part of an internal toolchain or automation system.

### 🔹 Vulnerability Demonstrated

The app:

- **Exports a broadcast receiver**:
  `com.victimapp.MySensitiveReceiver`

- Protects it using a **custom permission**:
  ```xml
  <uses-permission android:name="com.victimapp.SENSITIVE_PERMISSION" />
  ```

- Performs sensitive actions (vibration, toast, app launch) on receiving specific broadcast payloads.

However:

- The permission is not signature-protected
- Any app that declares the same permission can send broadcasts
- This makes the receiver abusable by untrusted third-party apps

➡️ See [`victimapp/README.md`](./victimapp/README.md)

---

## 🚨 Attacker App: Broadcast Replayer

A minimal app designed to abuse the exposed receiver in the Victim App.

### 🔹 Purpose

This app demonstrates how a malicious actor can:

- Replay a legitimate broadcast payload
- Bypass weak custom permission protection
- Continuously trigger the victim's sensitive behavior, such as:
  - Vibrating the device
  - Showing a toast
  - Auto-launching the victim app
  - Clearing of cache (Not demo-ed but implied)

### 🔹 Behavior

The app sends:

```kotlin
val intent = Intent("com.victimapp.CLEANUP")
intent.component = ComponentName("com.victimapp", "com.victimapp.MySensitiveReceiver")
intent.putExtra("payload", "LEGIT_CLEANUP")
intent.putExtra("caller_package", "com.attackerapp")
sendBroadcast(intent)
```

...repeatedly, every few seconds.

➡️ See [`attackerapp/README.md`](./attackerapp/README.md)

---

## 🎯 Goal of the Demo

This setup simulates a realistic but insecure pattern found in some Android apps — where trusted features are exposed publicly, and custom permissions are not enforced properly.

It showcases the importance of:

- Signature-level protections
- Internal-only receivers (`android:exported="false"`)
- Strict validation of incoming intents and caller identity

---

## 🧪 Tested Environment

| Property             | Value                          |
|----------------------|-------------------------------|
| Android Version      | 9.0 (API 28, Pie)              |
| Emulator             | Android Studio AVD             |
| Device Architecture  | arm64-v8a                      |

---

## 🛡️ Security Lesson

**Always assume exported components can be discovered.**
Use signature-level permissions and internal-only designs for sensitive functionality.

---

## 🎬 Video Demo

Watch the demonstration of the broadcast receiver vulnerability in action:



*The video shows how the attacker app continuously triggers the victim app's exposed broadcast receiver, causing repeated vibrations, toasts, and app launches. It also includes possible mitigation controls.*

### Alternative viewing methods:

If the video doesn't play directly in GitHub, you can:
- Download the file: [`ExposedReceiverDemo.MOV`](./ExposedReceiverDemo.MOV)
- View it locally after cloning the repository

