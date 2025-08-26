# Preventing App Data Exfiltration via Android Backups (`android:allowBackup`)


## TL;DR
If `android:allowBackup="true"` your app’s private data (shared prefs, files, DBs) can be automatically backed up (e.g., to Google Drive) and, on older Android versions or debug builds, extracted locally with `adb backup`. This risks sensitive data (tokens, cookies, cached secrets) being exposed. **Mitigate by setting `android:allowBackup="false"` and shipping `android:debuggable="false"` in release builds.**

---

## 🎬 Video Demo

Watch the threat demonstration of allowing backups for release builds in action:


---

## Table of Contents
- [Background](#background)
- [Risk](#risk)
- [Demonstration (PoC)](#demonstration-poc)
  - [Android 11 and lower](#android-11-and-lower)
  - [Android 12 and higher](#android-12-and-higher)
- [Control & Implementation](#control--implementation)
- [Validation](#validation)
---

## Background
Android provides **Auto Backup** (file-based) and **key-value backup** mechanisms that can include most app-private files unless the app opts out. Auto Backup uploads to the user’s Google Drive under their account; apps get up to ~25 MB per user by default.

This behavior is controlled by manifest attributes on the `<application>` element, notably `android:allowBackup`. If enabled (or not explicitly disabled), your app’s data may be backed up/restored.

Starting with **Android 12 (API 31)**, Google **restricted `adb backup`** by excluding app data by default for apps targeting 31+, reducing casual on-device exfil paths—but debug builds can still opt in.

---

## Risk
- **Cause**: App allows backup (`android:allowBackup="true"`), or debug build enables backup extraction.
- **Effect**: Sensitive data (tokens, cookies, cached secrets, user data) can leave the device via cloud backup or local ADB extraction on older versions/debug builds.
- **Real-world signal**: Bugs around logging/backup have exposed credentials in the wild (e.g., Slack Android password issue).

---

## Demonstration (PoC)

> ⚠️ **Legal/Safety**: Perform only on test devices/accounts you control.

### Android 11 and lower
1. In `AndroidManifest.xml`:
```xml
<application
    android:allowBackup="true"
    ... />
```
2. Create a local backup of the app’s data:
```bash
adb backup -noapk -noencrypt -f mobileapp_backup.ab com.example.app
```
3. Extract with Android Backup Extractor (ABE):
```bash
java -jar abe.jar unpack mobileapp_backup.ab mobileapp_backup.tar <password_if_any>
tar -xf mobileapp_backup.tar -C extracted/
```

4. Verify exfiltration: Inspect `extracted/` for shared pref XML, SQLite DBs, cached files, webview data, etc.

---

## Android 12 and higher

Apps targeting 31+ are excluded from `adb backup` by default. To reproduce for debugging only, you can temporarily ship a debug build that opts in.

In a debug manifest/build:
```xml
<application
    android:allowBackup="true"
    android:debuggable="true"
    ... />
```

Repeat the backup/extract steps above and verify data presence (expect success only on debuggable builds or apps targeting `<31`).

---

## Control and Implementation

**Disallow backups for release builds.**

**Goal**: Remove the exfiltration path by opting out of backup/restore and disallowing debug flags in production.

In `AndroidManifest.xml` (release):
```xml
<application
    android:allowBackup="false"
    android:debuggable="false"
    ... />
```

In `build.gradle`:
```gradle
buildTypes {
    release {
        debuggable false
    }
    debug {
        debuggable true
        // manifestPlaceholders = [ allowBackup:"true" ] // if needed for testing
    }
}
```

---

## Validation

**Before (vulnerable PoC)**
- `allowBackup=true` → `adb backup` produces `.ab` with prefs/DBs/files on Android ≤11; debug builds on 12+ can still be exfiltrated.

**After (mitigated)**

- `allowBackup=false` + `debuggable=false`
    - Android ≤11: `adb backup` contains no app data.
    - Android 12+: App data excluded by default; with non-debuggable release, exfil fails.
