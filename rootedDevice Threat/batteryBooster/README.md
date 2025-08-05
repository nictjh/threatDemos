# 🔋 Battery Booster Pro – Android Root DoS Demo

**Battery Booster Pro** is a simulated Android utility app designed to demonstrate a **root-level Denial of Service (DoS)** attack targeting session persistence files of sensitive apps — specifically [**android-mobile-banking app**](https://github.com/nictjh/android-mobile-banking-app/tree/threatDemo/changingTokensWRoot).

> 🎯 **VICTIM APP DEMO**: The target banking app used in this demonstration can be found here:
> **[📱 Android Mobile Banking App - Threat Demo Branch](https://github.com/nictjh/android-mobile-banking-app/tree/threatDemo/changingTokensWRoot)**

---

## 🎯 Threat Demonstrated

This proof-of-concept (PoC) app shows how a **rooted device** can be exploited to:

- Tamper with sensitive app data (even from sandboxed apps)
- Break session continuity by corrupting persistent storage
- Cause **forced logouts and user disruption**, creating a **high-friction experience**

---

## ⚙️ How It Works

- The victim app ([**android-mobile-banking app**](https://github.com/nictjh/android-mobile-banking-app/tree/threatDemo/changingTokensWRoot)) stores persistent auth/session data in:
  ```
  /data/data/com.ahmadsyuaib.androidmobilebankingapp/databases/RKStorage
  ```

- **Battery Booster Pro** uses `su` privileges to:
  - Locate this file
  - Inject binary junk (`dd` or `echo -e '\x00\xFF...'`) into the file
  - Corrupt its structure so the victim app can't deserialize the session
  - Repeats this process periodically to cause **recurring logouts**

---

## 🧪 Tested Environment

| Property             | Value                                      |
|----------------------|--------------------------------------------|
| Android Version      | Android 10 (Q)                             |
| Device Rooted        | ✅ Yes (Magisk or custom rooted AVD)       |
| Architecture         | `arm64-v8a`                                |
| Target File          | `/data/data/com.ahmadsyuaib.androidmobilebankingapp/databases/RKStorage` |

---

## 🚧 Simulated Behavior

Once installed on a rooted device:

1. **Runs silently in background**, pretending to  optimise phone's battery
2. Periodically executes: command to inject binary junk into RKStorage
3. **User experiences forced logout** when the banking app attempts to restore session

This behavior mimics an **intentional annoyance vector** — not stealing credentials, but **undermining user experience** persistently.

---

## ⚠️ Why This Matters

- Many mobile banking apps rely on **unprotected storage mechanisms** like SQLite or MMKV
- On rooted devices, malicious apps can bypass sandboxing and **tamper with critical app state**
- The attack **doesn’t require UI interaction** — it happens in the background

---

## 🔐 Mitigation Recommendations

- Detect and block rooted environments using libraries like RootBeer / SafetyNet / Play Integrity API

---
## 🧪 Conclusion

**Battery Booster Pro** represents a subtle yet dangerous category of Android malware:

Apps that don’t exfiltrate data — but instead **undermine user trust and experience** by corrupting sensitive app logic from behind the scenes.