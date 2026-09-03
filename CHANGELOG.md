# Changelog

## [2.0] - 2026-09-03

### Features

- improve wallpaper download notifications ([aa8aec1](https://github.com/Darkstar085/Wallora/commit/aa8aec1fe890aa40e39fcd5d5559fa69fc226351))
  - Tap the notification to open the saved wallpaper in Gallery
  - Show a wallpaper preview when the notification is expanded
  - Add quick actions to open and share the downloaded wallpaper
  - Keep notifications useful for both default and custom save locations
- show download notifications ([e47b908](https://github.com/Darkstar085/Wallora/commit/e47b908234e800caaf74c4685434877f339f1360))
  - Show a quick-panel notification when a wallpaper finishes downloading
  - Ask for notification permission when needed
  - Let the notification open Wallora
- swipe between wallpapers in preview ([92348de](https://github.com/Darkstar085/Wallora/commit/92348de8e616a585fabad55d29ac29dcb1b61bc4))
  - Add horizontal preview swiping between wallpapers
  - Preserve preview controls while paging
- add preview info and wallpaper downloads ([9c63645](https://github.com/Darkstar085/Wallora/commit/9c636457d6059a6e7f6980bc2f0321cc07512d78))
- improve wallpaper preview controls ([3dfe22e](https://github.com/Darkstar085/Wallora/commit/3dfe22efa9dfdcdefce45ef2bc05c3a469435d86))
  - Handle system back gestures without closing the app
  - Improve preview back button positioning
  - Let users choose home, lock, or both when applying wallpaper
  - Remove wallpaper target selection from Settings

### Fixes

- detect version changes for releases ([cf4ddc5](https://github.com/Darkstar085/Wallora/commit/cf4ddc59478225c6a638a5b010279a12cf737014))
  - Fix version change detection in the release script
  - Allow version bumps to start the release build correctly
- keep home order when opening previews ([966dabc](https://github.com/Darkstar085/Wallora/commit/966dabcf2fd093486cdf0dc891a1669b5b3dc885))
  - Keep Home and the other pages alive while a wallpaper preview is open
  - Returning from a preview keeps the same wallpaper order and scroll position
  - Avoid refreshing the Home page just because a preview was opened
- keep pages in place when switching tabs ([08cf6ea](https://github.com/Darkstar085/Wallora/commit/08cf6ea012397cba2ca88da4b2d90c55c03aa981))
  - Keep Home, Favorites, and Settings exactly where you left them
  - Switching tabs no longer reloads the page
  - Your Home scroll position and selections stay in place
- keep image cache bounded and OS-managed ([75ffe9f](https://github.com/Darkstar085/Wallora/commit/75ffe9fa1bb980f27e80c55ac9fa1a51bbf8590d))
- load wallpaper catalog only once per app session ([00f1640](https://github.com/Darkstar085/Wallora/commit/00f1640d9c78dd886848446a578327daf35b70fc))
  - Load the wallpaper catalog once per repository session
  - Randomize the catalog once when it is first loaded
  - Keep the randomized order stable across tab and category changes
  - Randomize the persisted fallback catalog once when the network is unavailable
- render version changelogs correctly ([1fc5567](https://github.com/Darkstar085/Wallora/commit/1fc55678c871f9848fd13aa36669857129b4c247))
  - Scope release notes to the selected version section only
  - Normalize commit SHAs before rendering changelog entries
  - Link short commit SHAs to their full GitHub commit pages
  - Preserve older release sections for Full Changelog comparisons

### UI / UX

- redesign Explore discovery layout ([30b064c](https://github.com/Darkstar085/Wallora/commit/30b064c52fa5325ff14318f1a58746b26206010c))
  - Add visual category browsing with representative wallpaper previews
  - Add featured and trending horizontal wallpaper sections
  - Keep search and category filtering integrated with the new layout
  - Preserve the latest wallpaper two-column grid and favorites behavior
  - Fix horizontal wallpaper favorite icon imports for debug and release builds

### Refactoring

- remove preview metadata strip ([0365ad6](https://github.com/Darkstar085/Wallora/commit/0365ad63237c97361696f77fc24c8caa62da0874))
  - Remove metadata from the preview surface
  - Keep detailed wallpaper information available through Info
- move UI values into resources ([d67d5b4](https://github.com/Darkstar085/Wallora/commit/d67d5b41ddf5d49c46e40a44838ab539be0f8c83))
  - Extract user-facing strings into strings.xml
  - Move explicit colors into colors.xml and UI dimensions into dimens.xml
  - Centralize timing and placeholder counts in integer resources
  - Update existing Compose screens to consume resource-backed values
- remove Explore tab and screen ([9071e56](https://github.com/Darkstar085/Wallora/commit/9071e563e1b910a612d20ca8113f82fa6ec0da07))
  - Remove Explore from the bottom navigation
  - Delete the Explore screen and its discovery UI
  - Keep Home, Favorites, and Settings as the primary destinations

### CI / Build

- simplify app versioning and release flow ([8937e61](https://github.com/Darkstar085/Wallora/commit/8937e61f231a4f128379f91d67c92533a818bd1d))
  - Keep app versionName and versionCode in one small version file
  - Start releases when version.properties changes
  - Keep Telegram build updates linked to the exact commit
  - Send APKs without extra caption text
  - Make release version checks work reliably in GitHub Actions
  - Keep release notes tied to the matching changelog entry

### Maintenance

- bump app version to 2.0 ([072c845](https://github.com/Darkstar085/Wallora/commit/072c8458fe3e49209981ff686b25dd5fc21faff0))
  - Update the app version to 2.0
  - Increase the version code for the new release

## [1.5] - 2026-09-02

### Fixes

- use persistent signing key for all APK builds (
126156)
  - Sign debug and release variants with the same Wallora keystore
  - Keep the signing credentials in environment variables only
  - Allow debug and release APKs to update each other without signature mismatch
- generate version-only release changelogs (
917158)
  - Show only commits between the current release and previous release
  - Add short commit SHAs to changelog entries
  - Exclude release version-bump commits from generated notes
  - Add a GitHub-style Full Changelog comparison link to release notes
  - Update existing releases when publishing again

### UI / UX

- move explore search into top bar (
b9d2af)
  - Remove the permanent Explore search field
  - Add a search icon to the top-right of the Explore header
  - Reveal the search field when the icon is opened
  - Preserve existing wallpaper search and category filtering

### CI / Build

- sign GitHub releases with persistent keystore (
3cc623)
  - Decode the release keystore from GitHub Secrets
  - Build release APKs with the persistent Wallora signing key
  - Fail the release when the signing key is missing
- sign debug builds with release key (
04d207)
  - Decode the release keystore from GitHub Secrets
  - Build debug APKs with the persistent signing key
  - Allow debug builds to install over existing signed releases

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
