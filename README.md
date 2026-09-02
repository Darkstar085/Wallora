# Wallora

A focused Android wallpaper app built with Kotlin and Jetpack Compose.

Wallora keeps the experience simple: discover curated wallpapers, search by title, filter by collection, save favorites, preview a wallpaper, and set it on the device.

## Stack

- Kotlin
- Jetpack Compose + Material 3
- Coil 3 for image loading
- OkHttp for the wallpaper catalog
- SharedPreferences for lightweight favorites
- GitHub Actions for debug APK builds

## Wallpaper catalog

The app reads the public catalog from `Darkstar085/Wallpapers`:

`api/wallpapers.json`

Image URLs are generated against the `main` branch so catalog metadata and committed files stay aligned.

## Screens

- Home — featured wallpaper, collections, latest wallpapers
- Explore — search and category filtering
- Favorites — saved wallpapers
- Preview — full wallpaper view and set-wallpaper action
- Settings — source and app information

## Build

Open the project in Android Studio with JDK 17 and an Android SDK that supports API 36.

Or run:

```bash
gradle assembleDebug
```

The GitHub Actions workflow also builds `assembleDebug` on every push to `main` and uploads the APK as an artifact.

## License

Wallora is developed in the `Darkstar085/Wallora` repository. The application source is maintained independently; external projects are used only as architectural references unless their license permits reuse.

Wallpaper files are maintained separately in `Darkstar085/Wallpapers` and remain subject to their respective source and repository licensing terms.
