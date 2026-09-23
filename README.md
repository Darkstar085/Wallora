# Wallora

A focused Android wallpaper app built with Kotlin and Jetpack Compose.

Wallora keeps the experience simple: discover curated wallpapers, filter by collection, save favorites, preview a wallpaper, and set it on the device.

## Stack

- Kotlin
- Jetpack Compose + Material 3
- Coil 3 for image loading
- OkHttp for the wallpaper catalog and image delivery
- SharedPreferences for lightweight favorites
- GitHub Actions for debug APK builds and releases

## Wallpaper catalog

The app reads the public catalog from `Darkstar085/Wallpapers`:

`api/wallpapers.json`

Image URLs are generated against the `main` branch so catalog metadata and committed files stay aligned.

## Screens

- Home — featured wallpaper, collections, and latest wallpapers
- Favorites — saved wallpapers
- Preview — full wallpaper view, download, and set-wallpaper actions
- Settings — source and app information

## Build

Open the project in Android Studio with JDK 17 and an Android SDK that supports API 37.

Or run:

```bash
./gradlew assembleDebug
```

The GitHub Actions debug workflow is manually triggered and sends the built APK to Telegram only. It does not upload a workflow artifact or send separate build notifications.

Release builds are triggered when `version.properties` changes on `main`. The existing release signing configuration is preserved, and release notes are generated from the matching `CHANGELOG.md` version section.

## License

Wallora is developed in the `Darkstar085/Wallora` repository. The application source is maintained independently; external projects are used only as architectural references unless their license permits reuse.

Wallpaper files are maintained separately in `Darkstar085/Wallpapers` and remain subject to their respective source and repository licensing terms.
