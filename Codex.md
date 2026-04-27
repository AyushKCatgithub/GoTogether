# GoTogether Codex Guide

This file is a quick orientation note for future Codex sessions working in this repo.

## Project Snapshot

GoTogether is a single-module Android app built with Kotlin, Jetpack Compose, Hilt, and Firebase.

- Module: `:app`
- Package: `com.quantax.gotogether`
- UI: Jetpack Compose + Material 3
- DI: Hilt
- Auth: Firebase Auth with Google Sign-In and phone OTP
- Data: Firestore, Firebase Realtime Database, Firebase Storage
- Images: Coil with an app-level `ImageLoaderFactory`
- App Check: Debug provider in debug builds, Play Integrity in release builds

## Important Files

- `settings.gradle.kts`: single `:app` module setup
- `build.gradle.kts`: root Gradle plugin aliases
- `app/build.gradle.kts`: Android, Compose, Hilt, Firebase dependencies and build config
- `gradle/libs.versions.toml`: version catalog
- `app/src/main/AndroidManifest.xml`: permissions, app class, main activity
- `app/src/main/java/com/quantax/gotogether/MainActivity.kt`: Compose entry point
- `app/src/main/java/com/quantax/gotogether/GoTogetherApplication.kt`: Firebase/App Check init and Coil config
- `app/src/main/java/com/quantax/gotogether/di/AppModule.kt`: Hilt providers
- `app/src/main/java/com/quantax/gotogether/navigation/NavGraph.kt`: root navigation graph
- `app/src/main/java/com/quantax/gotogether/navigation/Routes.kt`: route constants/builders
- `storage.rules`: Firebase Storage rules

## Package Layout

- `auth/`: `AuthManager`, wrapping Firebase Auth, Google Sign-In, and OTP.
- `data/repository/`: Firebase data access for trips, profiles, bookmarks, chat, join requests, notifications, and Storage.
- `domain/model/`: Firebase-friendly data classes: `Trip`, `UserProfile`, `Chatroom`, `ChatMessage`, `JoinRequest`, `Notification`.
- `presentation/`: feature screens and ViewModels.
- `presentation/components/`: shared Compose components.
- `ui/theme/`: Compose theme, colors, type.
- `utils/`: connectivity and permission helpers.

## Feature Areas

- `presentation/home`: trip feed, search/filtering, bookmarks, location-aware sorting.
- `presentation/trip`: create trip and trip detail flows.
- `presentation/profile`: own profile, edit profile, public profile.
- `presentation/auth`: sign-in and OTP verification.
- `presentation/chat`: chatroom UI and message handling.
- `presentation/messages`: chatroom list/messages entry.
- `presentation/notifications`: join request and notification handling.
- `presentation/mytrips`: hosted/bookmarked trip views.
- `presentation/onboarding`: splash/onboarding.
- `presentation/main`: main shell/bottom navigation.

## Firebase Data Notes

- Firestore collections used by repositories:
  - `trips`
  - `users`
  - `usernames`
  - `chatrooms`
  - `join_requests`
  - `notifications`
- Realtime Database path:
  - `chatrooms/{chatroomId}/messages`
- Storage folders:
  - `profile_images/{uid}/...`
  - `verification_photos/{uid}/...`
  - `trip_images/{uid}/...`
  - `chat_attachments/{uid}/...`

Trip and bookmark repositories enrich trip cards with host profile data from `users`.

## Commands

Run unit tests:

```sh
./gradlew testDebugUnitTest
```

Build/debug compile check:

```sh
./gradlew assembleDebug
```

Run all available checks:

```sh
./gradlew check
```

Note: Gradle may need access to `~/.gradle` for wrapper/cache files.

## Current Test Coverage

Tests are currently concentrated around profile behavior:

- `ProfileViewModelTest`
- `ProfileStateTest`
- `PublicProfileViewModelTest`
- default example tests

Good next test targets:

- `HomeViewModel` listener/retry behavior
- `TripDetailViewModel` combined trip/status/bookmark state
- join request approval/denial behavior
- bookmark toggling
- chat delete-for-me/delete-for-all behavior

## Previously Fixed Risk Areas

1. `HomeViewModel.loadTrips()` now cancels the previous trips collector before starting a new one.

2. `TripDetailViewModel.loadTrip()` now cancels the previous combined trip/status/bookmark collector and profile fetch before reloading.

3. `storage.rules` now denies unmatched Storage paths by default:

```js
match /{allPaths=**} {
  allow read, write: if false;
}
```

The named folders remain user-scoped.

4. The global `android:usesCleartextTraffic="true"` manifest setting was removed.

5. Debug signing credentials are no longer hardcoded. To use `app/debug_keystore.jks`, provide these Gradle properties locally:

```properties
GOTOGETHER_DEBUG_STORE_PASSWORD=...
GOTOGETHER_DEBUG_KEY_PASSWORD=...
GOTOGETHER_DEBUG_KEY_ALIAS=...
```

6. Compose screens now avoid guarded `!!` assertions in the previously flagged UI paths.

## Coding Guidance

- Follow existing feature-package organization under `presentation/*`.
- Prefer small ViewModel changes with focused tests when changing state behavior.
- Keep Firebase repository APIs coroutine/Flow-based, matching the current style.
- Be careful with long-lived Firebase listeners; retain jobs when reload/retry is possible and cancel previous collectors.
- Do not rewrite user-local changes. This repo may have a dirty worktree.
- Use `rg` for searching.
- Prefer `apply_patch` for manual edits.

## Last Verified

- `./gradlew testDebugUnitTest` passed after the risk cleanup.
- Existing dirty files at that time:
  - `.DS_Store`
  - `.idea/misc.xml`
  - `app/src/main/java/com/quantax/gotogether/presentation/profile/ProfileScreen.kt`
