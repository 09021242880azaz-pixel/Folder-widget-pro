# Folder Pro — Android Studio project

Folder Pro is an offline-first starter project for an Android home-screen folder widget.

## Included in this starter
- App screen to choose installed apps for the folder.
- Saved folder title, app selections, and square/round style.
- Android home-screen widget with a title and app grid.
- App shortcuts launch directly from the widget.
- Preferences stored locally on the device; no account or network permission is requested.

## Build
1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this folder (`FolderProAndroid`) in Android Studio.
3. Allow Gradle sync to complete.
4. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The APK is normally at `app/build/outputs/apk/debug/app-debug.apk`.

This project does not include a Gradle wrapper. Android Studio will use its supported Gradle tooling; if building from a terminal, create a wrapper with a compatible Gradle 8.9 installation (`gradle wrapper --gradle-version 8.9`).

## Add the widget
After installing, long-press the Android home screen, open **Widgets**, and add **Folder Pro**. Some launchers may require reopening the widget after changing settings.

## Notes
- This is a functional starter, not a byte-for-byte clone of Folder Widget.
- Android launchers control widget resizing, background rendering, and some animations.
- App discovery is limited to launchable apps visible to Android's package-visibility rules.
- The selected-app list is shared by Folder Pro widgets in this starter version.
