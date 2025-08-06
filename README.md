# 🚨 Android Security Threat Demonstrations Repository

> **A comprehensive collection of proof-of-concept Android security vulnerabilities and attack vectors**

This repository serves as an educational resource showcasing **real-world Android security threats** and their corresponding **mitigation strategies**. Each demonstration includes working code examples, detailed attack vectors, and security controls to help developers understand and prevent these vulnerabilities in production applications.

---

## 🎯 Repository Objectives

- **🔍 Demonstrate real Android vulnerabilities** with working proof-of-concept code
- **🛡️ Provide security controls** and mitigation strategies for each threat
- **📚 Educate developers** on common Android security pitfalls
- **🧪 Validate security tools** like MobSF and their vulnerability detection capabilities
- **⚡ Research cutting-edge** Android exploitation techniques

---

## 🗂️ Threat Categories Overview

### 🏆 **Featured Demo: Janus Vulnerability (CVE-2017-13156)**
> **My most comprehensive and technically advanced demonstration**

A complete reproduction of the infamous **Janus signature bypass vulnerability** that affected Android 6.0 and earlier versions.

**What it demonstrates:**
- **Dual-face APK construction** where PackageManager and ART Runtime parse the same file differently
- **Signature verification bypass** allowing malicious code execution with valid certificates
- **Custom Python toolchain** for APK manipulation and exploitation
- **Real-world validation** of MobSF's `minSdkVersion` security warnings

**Technical achievements:**
- ✅ Complete APK signature bypass on Android 6.0
- ✅ Malicious DEX injection with legitimate signature preservation
- ✅ Custom exploitation scripts (`make_janus_proper_old.py`, `scam_android.py`)
- ✅ Detailed forensic analysis with logcat evidence

[📁 **View Janus Vulnerability Demo →**](./JanusVuln/)

---

## 📦 Complete Threat Catalog

### 1. 🎭 **Application Impersonation & Phishing**

| Threat | Description | Demo Apps |
|--------|-------------|-----------|
| **Fake Banking App** | OCBC Bank UI clone with tampered backend endpoints | [`FakeApp/ocbcClone/`](./FakeApp/ocbcClone/) |

**Attack Vector:** APK decompilation → endpoint modification → repackaging → sideloading
**Impact:** Credential harvesting, financial fraud
**Mitigation:** App signing verification, secure distribution channels

---

### 2. 📡 **Broadcast Receiver Exploitation**

| Component | Description | Demo Apps |
|-----------|-------------|-----------|
| **Victim App** | Device cache cleaner with exposed sensitive receivers | [`Exposed BroadCastReceiver Threat/victimapp/`](./Exposed%20BroadCastReceiver%20Threat/victimapp/) |
| **Attacker App** | Malicious app exploiting weak broadcast permissions | [`Exposed BroadCastReceiver Threat/attackerapp/`](./Exposed%20BroadCastReceiver%20Threat/attackerapp/) |

**Attack Vector:** Custom permission bypass → broadcast replay → unauthorized actions
**Impact:** Remote triggering of sensitive functionality, DoS attacks
**Mitigation:** Signature-level permissions, internal-only receivers

[📁 **View Broadcast Receiver Demo →**](./Exposed%20BroadCastReceiver%20Threat/)

---

### 3. 🗂️ **External Storage Data Leakage**

| Component | Description | Demo Apps |
|-----------|-------------|-----------|
| **Secure Logger** | Encrypted credential logging with Android Keystore | [`READ_EXTERNAL_STORAGE Threat/mobile/`](./READ_EXTERNAL_STORAGE%20Threat/mobile/) |
| **Malicious Scanner** | Cookie Clicker game that secretly harvests external files | [`READ_EXTERNAL_STORAGE Threat/loggerMobile/`](./READ_EXTERNAL_STORAGE%20Threat/loggerMobile/) |

**Attack Vector:** `READ_EXTERNAL_STORAGE` permission abuse → recursive file scanning → data exfiltration
**Impact:** Sensitive data exposure, credential theft
**Mitigation:** AES encryption, Android Keystore, scoped storage (API 29+)

[📁 **View External Storage Demo →**](./READ_EXTERNAL_STORAGE%20Threat/)
[📁 **View WRITE_EXTERNAL_STORAGE Demo too! →**](https://github.com/nictjh/android-mobile-banking-app/blob/proof/ScopedStorage/README.md)

---

### 4. 🔓 **Rooted Device Exploitation**

| Threat | Description | Demo Apps |
|--------|-------------|-----------|
| **Root-based DoS** | Battery optimizer that corrupts banking app session files | [`rootedDevice Threat/batteryBooster/`](./rootedDevice%20Threat/batteryBooster/) |

**Attack Vector:** Root privilege escalation → file system tampering → session corruption
**Impact:** Forced logouts, user experience degradation, app instability
**Mitigation:** Root detection, SafetyNet/Play Integrity API, secure storage

[📁 **View Rooted Device Demo →**](./rootedDevice%20Threat/)

---

## 🛠️ Technical Implementation Details

### 🔧 **Development Stack**
- **Frontend:** React Native (Expo & Native)
- **Backend:** Supabase, Node.js
- **Security:** Android Keystore, AES encryption (`crypto-js`)
- **Tools:** Python exploitation scripts, APK manipulation tools
- **Testing:** Android Studio AVD, multiple API levels (23-36)

### 📱 **Tested Environments**
- **Android 6.0 (API 23)** - Janus vulnerability target
- **Android 9.0 (API 28)** - External storage demos
- **Android 10+ (API 29+)** - Rooted Environment
- **Android 16 (API 36)** - Modern Security Controls
- **Architectures:** `arm64-v8a`, `x86_64`

### 🧪 **Attack Simulation Framework**
Each demo includes:
- ✅ **Victim application** (vulnerable target)
- ✅ **Attacker application** (exploitation tool)
- ✅ **Step-by-step reproduction** guide
- ✅ **Security controls** and mitigation examples
- ✅ **Real-world impact** analysis

---

## 🎓 Educational Value

### 📚 **Learning Outcomes**
After exploring this repository, developers will understand:

1. **Common Android vulnerability patterns** and their root causes
2. **Exploitation techniques** used by real-world attackers
3. **Security controls** that effectively prevent these attacks
4. **Tool validation** - why security scanners flag certain patterns
5. **Secure development practices** for Android applications

---

## 🚀 Quick Start Guide

### 1️⃣ **Environment Setup**
```bash
# Clone the repository
git clone https://github.com/nictjh/threatDemos.git
cd threatDemos

# Install Android SDK and build tools
# Set up Android Studio AVD emulators
# Install Node.js, Python 3, React Native CLI
```

### 2️⃣ **Running Demonstrations**
Each threat category has its own setup instructions: refer to the directory's `README.md`

```bash
# Example: Janus Vulnerability
cd "JanusVuln/"
python3 make_janus_proper_old.py app-release23.apk nuclear_mainapp.dex janus-test.apk
python3 scam_android.py janus-test.apk janus-final.apk

# Example: External Storage Threat
cd "READ_EXTERNAL_STORAGE Threat/mobile/"
npm install
npm run android
```

### 3️⃣ **Security Testing**
Run each demo in controlled environments:
- Use **Android Studio AVD** for safe testing
- Monitor with **adb logcat** for exploitation evidence
- Analyze APKs with **MobSF** to understand security warnings

---

## 🏆 Research Highlights

### 🥇 **Most Advanced Demo: Janus Vulnerability**
- **Complete CVE-2017-13156 reproduction** on Android 6.0
- **Custom Python exploitation framework** with multiple iteration attempts
- **Real signature bypass** with working APK installation
- **Forensic evidence** captured in logcat demonstrating code execution

### 🎯 **Real-World Relevance**
- **Banking app phishing** with pixel-perfect UI clones
- **Permission abuse** scenarios affecting millions of devices
- **Root exploitation** targeting financial applications
- **Defense validation** proving security control effectiveness

---

## 🛡️ Security Recommendations Collated

### 🔒 **For Developers**
1. **Always set minimum SDK versions** to latest patched Android releases
2. **Use signature-level permissions** for sensitive components
3. **Implement proper encryption** for external storage
4. **Validate APK signatures** and use Play App Signing
5. **Detect rooted environments** in security-sensitive apps

---

## 📊 Demo Statistics

| Metric | Count |
|--------|-------|
| **Total Threat Categories** | 4 |
| **Demo Applications** | 8+ |
| **CVEs Demonstrated** | 1 (CVE-2017-13156) |
| **Android API Levels Tested** | 23, 28, 29, 36 |
| **Custom Exploitation Scripts** | 6+ Python tools |
| **Documentation Files** | 10 detailed READMEs |

---

## 🧑‍💻 Author & Research

**Crafted by Nicholas Tok**
*CSA Cybersecurity Engineer Intern*

*Built with hours of reverse engineering, emulator debugging, and security research.*

---

## ⚠️ Ethical Use Disclaimer

This repository is intended **solely for educational and research purposes**. All demonstrations should be:
- Used only in **controlled testing environments**
- Never deployed against **systems you don't own**
- Utilized to **improve security**, not exploit vulnerabilities
- Shared responsibly within the **cybersecurity community**

The author assumes no responsibility for misuse of these educational materials.

---

