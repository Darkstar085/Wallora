# Changelog

## [0.6.1] - 2026-09-02

No user-facing changes.

## [0.6.0] - 2026-09-02

### Features

- Redesign Wallora home screen
  - Remove the Home screen search bar, wallpaper metadata, wave divider, and top menu button
  - Add a shuffled featured wallpaper carousel with swipe navigation
  - Refresh the Home layout with image-first presentation
  - Replace the bottom navigation with a floating pill-style bar

- Add Wallora app icon
  - Add the selected geometric Wallora W launcher icon
  - Use a purple-to-blue gradient background without launcher borders
  - Scale the W for better launcher spacing
  - Register the adaptive launcher icon for Android
  - Add the matching repository icon

### CI / Build

- Improve build and release notifications
  - Notify Telegram when Wallora builds start and finish
  - Send APKs with the short commit SHA in the filename
  - Format Telegram APK captions with proper line breaks
  - Skip debug builds for version-only changes
  - Create releases only when version metadata changes

- Consolidate workflow fixes
  - Clean up failed and non-successful workflow runs
  - Use Telegram bot and chat secrets for APK delivery
  - Show Telegram upload progress and enforce transfer timeouts
  - Run builds only when build files change
  - Generate releases from the matching changelog version section
  - Keep workflow changes from triggering build or release runs

## [0.5.0] - 2026-09-02

### Added
- Added Home, Explore, Favorites, and Settings tabs with Material 3 UI.
- Added wallpaper browsing, search, category filtering, and loading states.
- Added persistent favorites and full-screen wallpaper preview.
- Added native wallpaper application for Home, Lock, or Home & Lock screens.
- Added System, Light, and Dark theme modes with optional Material You dynamic colors.
- Added persistent wallpaper and appearance preferences.

### Improved
- Redesigned Settings with compact Material 3 preference sections.
- Added the app version to Settings from `BuildConfig`.
- Added automated APK builds, artifacts, Telegram delivery, releases, and workflow cleanup.
