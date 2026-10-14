# FDFS Alert — Build Log

## Toolchain Configuration
- **OS**: Windows 11 Pro 64-bit
- **Java / JDK**: OpenJDK 25.0.3 (JetBrains Runtime)
- **Gradle**: 9.6.0
- **Android Gradle Plugin**: 9.4.1
- **Kotlin**: 2.1.0
- **KSP**: 2.1.0-1.0.29
- **Room**: 2.7.0-alpha13 / 2.6.1 with `room.generateKotlin = true`
- **Fragment KTX**: 1.8.5
- **Python**: 3.11.9 (`backend/.venv`)

## Build Fixes & Resolution
1. **Lint Fix**:
   - Issue: `InvalidFragmentVersionForActivityResult` in `MainActivity.kt`.
   - Fix: Added `implementation("androidx.fragment:fragment-ktx:1.8.5")` in `app/build.gradle.kts`.
   - Command: `.\gradlew.bat lint --no-configuration-cache`
   - Result: BUILD SUCCESSFUL (0 errors).

2. **Room / KSP Fix**:
   - Issue: `unexpected jvm signature V` & `Continuation` type mismatch.
   - Fix: Added `ksp { arg("room.generateKotlin", "true") }` in `app/build.gradle.kts` and explicit return types (`Int`/`Long`) in `Daos.kt`.

## Three Consecutive Debug Builds Verification
1. **Build 1**: Exit Code 0 | Duration: 32s | APK Size: 20,521,099 bytes | Timestamp: 10/11/2026 16:47:57
2. **Build 2**: Exit Code 0 | Duration: 4s  | APK Size: 20,521,099 bytes | Timestamp: 10/11/2026 16:47:57
3. **Build 3**: Exit Code 0 | Duration: 5s  | APK Size: 20,521,099 bytes | Timestamp: 10/11/2026 16:47:57

**Verified Artifact Path**:
`C:\Users\ADMIN\AndroidStudioProjects\MyApplication2\app\build\outputs\apk\debug\app-debug.apk`
