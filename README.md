# 🛡️ SecureVPN - Fast & Secure Android VPN Client

[![Android Version](https://img.shields.io/badge/Android-7.0%2B%20%28API%2024%2B%29-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-purple.svg)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**SecureVPN** is a modern, high-performance, and secure Android VPN application built with Kotlin, Jetpack Compose, and Material Design 3. It provides encrypted VPN tunneling, robust DNS protection, full theme customization, and custom WireGuard `.conf` configuration importing.

---

## 👨‍💻 Developer & Credits

**Designed & Developed by:** **Kavindu Bogahawatte**  
**Project:** Complete Android VPN Application  
**Copyright:** © 2025 Kavindu Bogahawatte. All rights reserved.

---

## ✨ Key Features

- **⚡ Instant & Reliable High-Speed Connectivity:** Built on native Android `VpnService` with protected socket routing (`protect(socket)`) ensuring uninterrupted fast internet access across 4G, 5G, and Wi-Fi.
- **🎨 Creative Modern UI:** Designed with Jetpack Compose & Material 3, featuring glowing connection rings, pulse animations, real-time connection timer (`PROTECTED • 00:04:12`), and custom app logo integration (`logo.jpg`).
- **🌗 Full Theme Mode Support:**
  - 📱 **System Default**
  - ☀️ **Light Mode**
  - 🌙 **Dark Mode**
  - Theme choices are stored securely in `SecureStorage` and restored seamlessly.
- **🛡️ DNS Leak Protection:** Enforces secure DNS resolution via Cloudflare (`1.1.1.1`) and Google (`8.8.8.8`) DNS servers.
- **📄 Custom WireGuard Config (.conf) Importer:** Built-in wg-quick configuration parser allowing users to easily import custom WireGuard profiles or edit server endpoints.
- **🔐 Hardware-Backed KeyStore Security:** Client keys and server credentials are encrypted with AES-256 GCM using the Android System KeyStore (`SecureStorage`).
- **🚨 System Kill Switch:** Integrated shortcut to open native Android VPN Settings for system-wide "Block connections without VPN" lockdown.

---

## 🛠️ Architecture & Tech Stack

| Component | Technology / Library |
|---|---|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose + Material Design 3 |
| **VPN Engine** | Native Android `VpnService` (`SecureVpnService`) |
| **State Management** | Kotlin Coroutines, `StateFlow`, `ViewModel` |
| **Security & Storage** | Android KeyStore (`AES/GCM/NoPadding`), `SharedPreferences` |
| **Notifications** | Android 14+ (API 34+) Foreground Service Notification |
| **Target SDK** | Android 16 (API Level 36) |
| **Minimum SDK** | Android 7.0 (API Level 24) |

---

## 📁 Project Structure

```text
SecureVPN/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/securevpn/
│   │       │   ├── MainActivity.kt            # Main entry Activity & permissions handler
│   │       │   ├── MainViewModel.kt           # UI State observer
│   │       │   ├── security/
│   │       │   │   └── SecureStorage.kt       # AES-256 GCM KeyStore encrypted storage
│   │       │   ├── ui/
│   │       │   │   ├── HomeScreen.kt          # Compose UI, Connection button & Server List
│   │       │   │   └── theme/                 # App Colors, Typography & Theme Modes
│   │       │   └── vpn/
│   │       │       ├── SecureVpnService.kt    # Native Android VpnService implementation
│   │       │       ├── VpnServiceManager.kt   # VPN Service Lifecycle Controller
│   │       │       ├── VpnConfig.kt           # VPN Config Data Model & parser
│   │       │       ├── VpnServers.kt          # Server profiles & wg-quick .conf parser
│   │       │       └── VpnStateStore.kt       # Global StateFlow for connection status
│   │       └── res/
│   │           ├── drawable/logo.jpg          # Official App Logo
│   │           └── values/
├── build.gradle.kts                           # Root Gradle build script
├── app/build.gradle.kts                       # App module dependencies
├── README.md                                  # Documentation
└── LICENSE                                    # MIT License File
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio:** 2026.1 or newer
- **JDK:** Java 17
- **Android SDK:** API Level 36 (Android 16)
- **Target Device:** Android 7.0+ (API 24+) physical device or emulator

### Build & Run Instructions
1. Clone or open the repository in Android Studio:
   ```bash
   git clone https://github.com/KavinduBogahawatte/SecureVPN.git
   cd SecureVPN
   ```
2. Build the Debug APK using Gradle:
   ```bash
   ./gradlew app:assembleDebug
   ```
3. Run the application on a connected device or emulator directly from Android Studio or via ADB:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 📜 License

This project is licensed under the **MIT License**.

```text
MIT License

Copyright (c) 2025 Kavindu Bogahawatte

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

### ❤️ Developed by Kavindu Bogahawatte
If you find this project helpful, please give it a ⭐ on GitHub!
