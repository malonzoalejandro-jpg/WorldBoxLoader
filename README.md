# WorldBox Loader

An Android companion app for managing WorldBox mod files.

## Current status

This repository is being set up. The initial Android app is intended to import and organize ZIP content packs and DLL files locally. Importing a DLL does **not** make it executable in WorldBox.

The Google Play Android release of WorldBox uses IL2CPP. Actual DLL mod execution requires a compatible, version-specific IL2CPP runtime bridge and game integration. This starter project does not yet patch the game, inject code, or load DLL mods.

## Planned build

The Android project will use Kotlin, Jetpack Compose, Android SDK 35, and a small native C++ scaffold. A GitHub Actions workflow will build a debug APK once the Android project files are committed.

## Safety

Only install modified APKs from sources you trust. Back up your game data before testing any patched build.
