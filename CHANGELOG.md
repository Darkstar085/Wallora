# Changelog

## [1.0] - 2026-09-02

### Features

- add persistent wallpaper cache controls
  - Persist downloaded wallpaper images in an app-owned disk cache
  - Reuse one ImageLoader with explicit memory and disk cache policies
  - Show cache size and clear cached images and metadata from Settings
- randomize home and sort explore by date
  - Randomize Home wallpapers on each app launch and across categories
  - Auto-advance the featured carousel every four seconds
  - Remove carousel position dots
  - Read wallpaper dates from the API and sort Explore by newest first
- cache wallpapers for offline use
  - Cache the latest wallpaper metadata locally after a successful fetch
  - Load cached wallpaper metadata when the network is unavailable
  - Reuse the application context for the wallpaper repository
- redesign Wallora home screen
  - Remove the Home screen search bar, wallpaper metadata, wave divider, and top menu button
  - Add a shuffled featured wallpaper carousel with swipe navigation
  - Refresh the Home layout with image-first presentation
  - Replace the bottom navigation with a floating pill-style bar
- add Wallora app icon
  - Add the selected geometric Wallora W launcher icon
  - Use a purple-to-blue gradient background without launcher borders
  - Scale the W for better launcher spacing
  - Register the adaptive launcher icon for Android
  - Add the matching repository icon

### Fixes

- randomize only all wallpapers

### CI / Build

- clean old workflow artifacts and caches
  - Keep only the newest workflow run for each workflow
  - Remove artifacts belonging to older workflow runs
  - Preserve artifacts from each workflow's latest run
  - Keep only the newest cache for each cache family
  - Preserve the latest dependency cache for reuse
  - Paginate workflow runs, artifacts and caches during cleanup
- automate changelog and release tooling
  - Generate release changelogs from git history
  - Amend generated changelogs into the triggering release commit
  - Update GitHub releases from the committed changelog
  - Consolidate Telegram and release workflow tooling in Python
  - Keep build and release workflows manual-only
  - Amend release commits as github-actions[bot]
- improve build and release notifications
  - Notify Telegram when Wallora builds start and finish
  - Send APKs with the short commit SHA in the filename
  - Format Telegram APK captions with proper line breaks
  - Skip debug builds for version-only changes
  - Create releases only when version metadata changes
- consolidate workflow fixes
  - Clean up failed and non-successful workflow runs
  - Use Telegram bot and chat secrets for APK delivery
  - Show Telegram upload progress and enforce transfer timeouts
  - Run builds only when build files change
  - Generate releases from the matching changelog version section
  - Keep workflow changes from triggering build or release runs
- configure Wallora workflows
  - Build debug APKs manually
  - Upload successful APKs as workflow artifacts
  - Send built APKs to Telegram after successful builds
  - Create GitHub releases when the app version changes
  - Generate release notes from changelog content and commit history
  - Keep the latest 3 GitHub Actions runs

### Build

- use persistent APK signing
  - Reset version metadata to 0.6.1
  - Configure CI signing from GitHub Actions secrets
  - Keep local debug builds on the default signer when secrets are absent

### Other

- bump app version to 1.0
  - Set the Android app version to 1.0
  - Increment version code for the major release

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
