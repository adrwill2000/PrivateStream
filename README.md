# PrivateStream Android

Personal, account-free YouTube client for a non-root Pixel 9 Pro XL. This is a GPL-3.0-or-later fork of LibreTube, not an official LibreTube release. Upstream authors, assets, LICENSE and notices are retained; see README.upstream.md.

## Implemented in this preview

- Real NewPipe-based search/extraction and Media3 playback inherited from LibreTube.
- Existing SponsorBlock, native Picture-in-Picture, background audio, local subscriptions/playlists and offline library.
- PrivateStream application identity: app.privatestream.debug for preview installs.
- Save video (up to 1080p by default) and Save audio buttons in portrait and landscape players. The original download options remain available.
- Completed downloads publish to Download/PrivateStream through MediaStore on Android 10+. Audio/video pairs are muxed with Media3; originals remain available offline. If export fails, the app reports it and retains originals for manual export.
- No analytics/telemetry integration added. Verbose HTTP logging disabled, including debug builds. Android backup disabled.
- Search/watch history, automatic updates, remote suggestions, subscription polling, ReturnYouTubeDislike and sync disabled by default. Direct extraction/playback; no video privacy proxy.
- Background API/images blocked. User download/media clients exempt; extractor requests permitted while a player or download service is active. This is purpose-based control, not a VPN/firewall.
- Privacy dashboard under Settings. A bounded in-memory host/purpose/request counter stores no full URLs, queries, headers, video IDs or bodies. Separate Media3 HTTP transport is outside counter coverage.
- Image disk caching disabled. Image memory cache, counts and temporary error text clear on normal exit/task removal. Memory is discarded by process death. Durable Room data and media remain intact.
- Task removal no longer deliberately stops background playback. Android may still stop the process/service.

## Build

Requirements: JDK 21, Android SDK platform 36 and compatible build tools. Gradle wrapper is included. Standard Android environment: `./gradlew :app:assembleDebug :app:testDebugUnitTest`.

When this checkout is accessed from Windows through its WSL UNC path, set `PRIVATESTREAM_WINDOWS_STAGE` to a local Windows directory. The script stages source there for the Windows Gradle wrapper, then copies the APK back into this checkout. Set `PRIVATESTREAM_TOOLCHAIN_HOME` and `GRADLE_USER_HOME` to reuse an existing JDK/Android SDK and Gradle cache; without those overrides it looks under `.private-build/`. Set `ANDROID_USER_HOME` to the directory containing your existing `debug.keystore` when you need update-compatible debug APKs; a different key will prevent Android from updating an installed build. For a local Windows protobuf launch incompatibility, `-PprivateStreamProtoc=ABSOLUTE_PATH` optionally selects a verified protoc executable (3.20.3 used here). Protobuf's Java lite generator is configured as a builtin, not an external plugin.

Opt-in live extraction tests: run `./build-private.ps1 -LiveExtraction`. They contact YouTube using real search and extraction; this option reruns tasks so Gradle cannot satisfy the live test from cache. Ordinary unit tests skip that test. APK: app/build/outputs/apk/debug/app-debug.apk.

## Status and limits

This is an early debug preview, not a Pixel 9 Pro XL-verified release. The latest build succeeded; 13 JVM tests passed, the opt-in live test was skipped, and there were no failures. A forced live extraction run confirmed real YouTube search and audio/video stream retrieval. On an Android 16 emulator, search and playback worked; one-tap audio and video downloads exported to shared `Download/PrivateStream`; and a downloaded video played from the offline library with network disabled. These emulator checks do not establish behavior on the Pixel. PiP, lock-screen/background continuity, SponsorBlock skips, and preservation of local data across process death still need device validation. Some videos, live streams, restricted content or YouTube changes can fail.

The exporter retains both a private offline copy and a public copy, increasing storage use. Removing an offline entry does not delete its public export. Muxing may take time or fail for incompatible formats. Shared downloads are not removed by clearing session data.

Source provenance, decisions, privacy scope and next validation steps: docs/BUILD_STATUS.md.
