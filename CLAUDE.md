# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Tack is a metronome Android app (package: `xyz.zedler.patrick.tack`) with two modules:
- **`app`** — Phone/tablet app (Java, View-based UI with Fragments)
- **`wear`** — Standalone Wear OS app (Kotlin, Jetpack Compose UI)

Both modules use native C++ audio via the Oboe library for low-latency sound generation.

## Build Commands

```bash
# Build debug APKs
./gradlew :app:assembleDebug
./gradlew :wear:assembleDebug

# Build release APK (requires KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD env vars for signing)
./gradlew :app:assembleRelease

# Run lint
./gradlew :app:lint
./gradlew :wear:lint

# Build both modules
./gradlew assembleDebug
```

Requires JDK 21 and Android NDK 29.0.14206865 (for native Oboe audio compilation via CMake).

## Architecture

### App Module (Java, Views)
- **MVVM pattern** with AndroidViewModel, LiveData, and Room database
- **Navigation**: Jetpack Navigation Component with SafeArgs. Nav graph at `app/src/main/res/navigation/nav_main.xml`
- **Activities**: `MainActivity` (hosts NavHostFragment), `SplashActivity`, `SongActivity`
- **Fragments**: `MainFragment` (metronome UI), `SongsFragment`/`SongFragment` (song library), `SettingsFragment`, `AboutFragment`, `LogFragment`
- **Database**: Room v3 with entities `Song` and `Part`, relation `SongWithParts`, accessed via `SongDao`
- **Service**: `MetronomeService` — foreground service for background playback
- **Widget**: `SongsWidgetProvider` with `SongsRemoteViewsService`
- **Native audio**: `AudioEngine.java` wraps JNI calls to `app/src/main/cpp/oboe_audio_engine.cpp`

### Wear Module (Kotlin, Compose)
- **Compose UI** with Wear Material, `SwipeDismissableNavHost` for navigation
- **Screens**: `MainScreen`, `TempoScreen`, `BeatsScreen`, `TapScreen`, `BookmarksScreen`, `SettingsScreen`, etc.
- **ViewModel**: `MainViewModel` uses StateFlow for reactive state
- **Service**: `MetronomeService` — foreground service (same pattern as app module)
- **Native audio**: Same Oboe-based `AudioEngine` with its own `wear/src/main/cpp/oboe_audio_engine.cpp`

### Shared Patterns
- Constants are **duplicated** between modules: `Constants.java` (app) and `Constants.kt` (wear) — not extracted into a shared module
- Both modules build their own `liboboe-audio-engine.so` native library via CMake
- `MetronomeEngine` handles timing/threading logic in both modules

## Key Technical Details

- **Language split**: App module is Java; Wear module is Kotlin with Compose
- **Min SDK**: App = 23 (Android 6.0), Wear = 26 (Wear OS 1.0)
- **Target/Compile SDK**: 36
- **Version catalog**: `gradle/libs.versions.toml` manages all dependency versions
- **ProGuard**: Both modules have `proguard-rules.pro` preserving Parcelable, native methods, and Gson-serialized classes
- **Translations**: Managed via Transifex, 16 locale configurations. Lint suppresses `MissingTranslation`
- **CI**: GitHub Actions (`deploy.yml`) builds signed release APK on version tags (`v*`), publishes to GitHub Releases
- **Fastlane**: Metadata and changelogs in `fastlane/` for store listings

## Conventions

- All source files include the GPLv3 license header (Copyright Patrick Zedler)
- Room schema exports go to `app/schemas/`
- Release signing uses environment variables, not checked-in keystores
- Version codes: last digit `0` = app release, last digit `1` = wear release
