# WorldBox Loader

An Android companion app for managing WorldBox mod files.

## Current status

This repository is being set up. The initial Android app is intended to import and organize ZIP content packs and DLL files locally. Importing a DLL does **not** make it executable in WorldBox.

The Google Play Android release of WorldBox uses IL2CPP. Actual DLL mod execution requires a compatible, version-specific IL2CPP runtime bridge and game integration. This starter project does not yet patch the game, inject code, or load DLL mods.

## Planned build

The Android project will use Kotlin, Jetpack Compose, Android SDK 35, and a small native C++ scaffold. A GitHub Actions workflow will build a debug APK once the Android project files are committed.

## Safety

Only install modified APKs from sources you trust. Back up your game data before testing any patched build.


## GameBanana browser (0.2)

The Browse tab searches WorldBox submissions through GameBanana's public API and opens the official mod page. Download a DLL from the mod author's page, then use **Import downloaded DLL** to add it to the local library.

### Current limitations

- The browser is a catalog/page launcher, not a one-click GameBanana file installer.
- Imported DLLs are stored inside this app's private storage. They are not copied into WorldBox's sandbox.
- DLL execution is not implemented. Android WorldBox builds using IL2CPP need a compatible runtime bridge and a game-specific integration. A normal Android app cannot inject code into another app's private process.
- Only use DLLs explicitly built for the exact WorldBox version and Android runtime. A Windows DLL is not an Android mod.

The official Android NeoModLoader documentation describes an existing path based on LemonLoader and a mobile NML build. This project is not using that loader; implementing an independent bridge remains separate work.
