# 🧨 Mobile App: Broadcast Spammer (Attacker App)

## 🎯 Purpose

This app simulates a **malicious actor** that takes advantage of an improperly protected **exported broadcast receiver** in another app [the Cache Cleanup App](https://github.com/nictjh/threatDemos/tree/master/Exposed%20BroadCastReceiver%20Threat/victimapp), **(Victim App)**.

Its goal is to demonstrate how an exported broadcast receiver with inadequate protection can be abused by third-party apps to trigger unintended behavior, even without special permissions.

---

## ⚠️ Behavior Demonstrated

The app targets an exported broadcast receiver in the Victim App:

```xml
com.victimapp.MySensitiveReceiver
```

The attacker app reuses known payloads intended for trusted callers, such as "LEGIT_CLEANUP", to trigger privileged behaviors repeatedly via an exported receiver.

```kotlin
intent.putExtra("payload", "LEGIT_CLEANUP")
```

By replaying a legitimate usage pattern, the app is able to:

- **Bypass weak custom permission checks**
- **Continuously trigger cache cleanup logic**
- **Cause the victim app to launch, vibrate, and display a toast**

---

## 💥 Spamming Capability

The app includes a **button-driven UI** that allows the user to:

- Start and stop continuous broadcasts every 3 seconds.
- Toggle spamming mode on and off.
- Observe when broadcasts are being sent through logs and optional toasts.

### Broadcast Intent Details:

| Field            | Value                      |
|------------------|----------------------------|
| `action`         | `"com.victimapp.CLEANUP"`  |
| `component`      | `com.victimapp.MySensitiveReceiver` |
| `payload`        | `"LEGIT_CLEANUP"`          |
| `caller_package` | `"com.attackerapp"`        |

---

## 🧪 Demo Flow

1. **User taps "Start Spamming"** in the attacker app.
2. Every 3 seconds, a broadcast is sent targeting the victim receiver.
3. Victim app receives the broadcast, believes it's from a trusted source, and executes:
   - Vibrate
   - Toast
   - Auto-launch
4. Attacker can stop spamming anytime.

---

## 🧬 Technical Behavior

Broadcasts are sent from:

```kotlin
val intent = Intent("com.victimapp.CLEANUP").apply {
    component = ComponentName("com.victimapp", "com.victimapp.MySensitiveReceiver")
    putExtra("payload", "LEGIT_CLEANUP")
    putExtra("caller_package", applicationContext.packageName)
}
sendBroadcast(intent)
```

Spamming logic:

```kotlin
handler.postDelayed(this, 3000) // Every 3 seconds
```

---

## 🌎 Demonstration Environment

| Component                | Value                            |
|--------------------------|----------------------------------|
| **Target App**          | Victim App with exported receiver |
| **Android Version**     | Android 9.0 (API 28)              |
| **Emulator Used**       | Android Studio Emulator           |
| **Architecture**        | arm64-v8a                         |

---

## 🚨 Key Takeaway

This app highlights how **poorly protected exported components** can be **exploited by any app** on the device — even without special permissions.

> 🔓 If you expose a component, **assume it will be targeted**.
