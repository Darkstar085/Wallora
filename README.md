# Wallora

<div align="center">

**A clean Android wallpaper app built with Kotlin and Jetpack Compose.**

Discover curated wallpapers, save favorites, preview images, download them, and set them directly on your device.

<p>
  <a href="https://github.com/Darkstar085/Wallora/releases"><img src="https://img.shields.io/github/v/release/Darkstar085/Wallora?style=flat-square" alt="Latest release"></a>
  <a href="https://github.com/Darkstar085/Wallora/actions"><img src="https://img.shields.io/github/actions/workflow/status/Darkstar085/Wallora/debug.yml?style=flat-square&label=debug" alt="Debug build"></a>
  <a href="https://github.com/Darkstar085/Wallora/blob/main/LICENSE"><img src="https://img.shields.io/github/license/Darkstar085/Wallora?style=flat-square" alt="License"></a>
</p>

</div>

## ✨ Features

- 🖼️ Curated wallpaper discovery and categories
- ❤️ Persistent favorites
- 🔍 Search and filtering
- 👀 Full-screen preview with wallpaper controls
- 📥 Downloads with Android notifications
- 🔄 In-app update checks from GitHub Releases

## 🧱 Stack

| Area | Technology |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Images | Coil 3 |
| Networking | OkHttp |
| JSON | Moshi |
| Background work | WorkManager |
| Build | Gradle + GitHub Actions |

## 🚀 Build

Requirements: Android Studio, JDK 17, Android SDK API 37, and Android 8.0 (API 26) or newer.

```bash
./gradlew assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`.

Release signing uses these environment variables:

```text
ANDROID_KEYSTORE_PATH
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_ALIAS
ANDROID_KEY_PASSWORD
```

App versioning lives directly in `app/build.gradle.kts`:

```kotlin
versionCode = 10
versionName = "2.0"
```

## 📦 GitHub Actions

| Workflow | Purpose |
| --- | --- |
| Debug | Build a debug APK and send it to Telegram |
| Release | Validate version, build/sign APK, publish GitHub Release, and send APK to Telegram |
| Cleanup | Retain the latest workflow run and configured cache family |

All three workflows are manually triggered.

Release notes are generated from Git commit subjects. `CHANGELOG.md` is intentionally not part of the release process.

## 🖼️ Wallpaper catalog

Wallora reads `api/wallpapers.json` from [`Darkstar085/Wallpapers`](https://github.com/Darkstar085/Wallpapers). The wallpaper repository and its image assets are maintained separately and may have their own licensing and attribution requirements.

## 📁 Structure

```text
Wallora/
├── app/                 # Android application
├── .github/scripts/     # CI/release helpers
├── .github/workflows/   # Manual GitHub Actions
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew
```

## 🤝 Contributing

Keep changes focused and use the repository's Conventional Commit style. For larger changes, open an issue before implementation.

## 📄 License

The Wallora application source is licensed under the [MIT License](LICENSE). Wallpaper assets are maintained separately.