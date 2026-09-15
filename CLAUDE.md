# CLAUDE.md

This file gives Claude Code context on this project's history and current state, since it was originally built via a chat session (claude.ai) that Claude Code cannot see directly.

## What this project is

A minimal, elegant black-and-white nonogram (Japanese crossword) Android app.
Kotlin + Jetpack Compose (Material 3), MVVM architecture. 100 puzzles (10x10, hand-curated + procedurally generated), local progress tracking via DataStore, Google sign-in scaffold via Firebase Auth + Credential Manager.

## How this repo was created

The entire codebase was written by Claude in a claude.ai chat session, then committed file-by-file into this repo through the GitHub web UI (browser automation), since that session had no direct git/filesystem access. There is no local git history of "how the code evolved" beyond the commit log itself -- every commit is essentially "add this one file." The design decisions and reasoning live only in that chat transcript, which is why this file exists: to carry forward the parts that matter.

## Known issues found and fixed during initial build

The GitHub Actions build failed twice on the first real compile; both are fixed now, but worth knowing about since similar mistakes are easy to reintroduce:

1. Missing `implementation("com.google.android.material:material:1.12.0")` in `app/build.gradle.kts`. Without it, AAPT fails to resolve `Theme.Material3.DayNight.NoActionBar` used in `res/values/themes.xml`. Compose apps still need this dependency if the manifest theme's parent comes from Material Components rather than pure Compose.
2. Missing `import androidx.compose.foundation.layout.width` in a couple of screen files (e.g. `PuzzleListScreen.kt`). Watch for this class of bug: since the code was typed into GitHub's web editor rather than an IDE, there was no autocomplete/autoimport, so any file that used `Modifier.width(...)` but only had `height`/`padding` imported would silently fail to compile until caught by CI.

If Claude Code encounters "Unresolved reference" errors in a freshly-touched screen file, check imports first -- this is the most likely culprit given how the file was originally authored.

## Build & CI

- `.github/workflows/build-apk.yml` builds a debug APK on every push to `main`/`master`, uploads it as artifact `nonogram-debug-apk`. This is the primary way to get an installable APK without a local Android SDK.
- Locally, `./gradlew assembleDebug` (needs JDK 17, Android SDK).
- `tools/generate_puzzles.py` regenerates `app/src/main/assets/puzzles.json` (100 puzzles: ~15 hand-authored recognizable shapes + procedurally generated symmetric patterns). Re-run it if the puzzle set needs to change; don't hand-edit the JSON.

## Google Sign-In status

Scaffolded but NOT functional yet. `app/google-services.json` and `AuthConfig.WEB_CLIENT_ID` (in `GoogleAuthManager.kt`) are placeholders. To make sign-in work: create a Firebase project, add the Android app (package `com.nonogram.mvp`), download the real `google-services.json`, enable Google as a sign-in provider, and paste the real Web Client ID into `AuthConfig`. Full steps are in `README.md`. Until then, the app works fully offline; only the "Continue with Google" button will error.

## Design intent (so it doesn't get lost)

- Deliberately not stark black/white or default Material colors: warm "paper" palette (`PaperBackground`, `InkPrimary`, etc. in `ui/theme/Color.kt`) for light mode, deep charcoal (not pure black) for dark mode, single restrained amber accent (`Accent`/`AccentDark`) used sparingly.
- Serif headings + clean sans body text (`ui/theme/Type.kt`), meant to avoid a "2000s app" feel.
- The nonogram grid itself (`ui/components/NonogramGrid.kt`) is drawn manually via Compose `Canvas`, not a library -- includes drag-to-paint, dimmed/"satisfied" clue styling when a row/column is already correctly filled.

## Working style note

Code in this repo was authored without an IDE (no compiler feedback while writing), so please actually build (CI or locally) after non-trivial changes rather than assuming it compiles -- there may be other small latent issues of the same "missing import" class that haven't surfaced yet because that code path hasn't been touched/compiled-tested individually.
