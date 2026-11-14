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

In this workspace on Windows: `./build-private.ps1 -LiveExtraction` uses the isolated toolchain in the sibling work directory. A source-only archive does not include that toolchain: set JAVA_HOME and ANDROID_HOME for an existing installation. For a local Windows protobuf launch incompatibility, `-PprivateStreamProtoc=ABSOLUTE_PATH` optionally selects a verified protoc executable (3.20.3 used here). Protobuf's Java lite generator is configured as a builtin, not an external plugin.

Opt-in live extraction tests: add `-PliveExtraction=true`. They contact YouTube using real search and extraction; ordinary unit tests skip that test. APK: app/build/outputs/apk/debug/app-debug.apk.

## Status and limits

This is an early debug preview, not a completed phone-verified release. The build and 12 JVM tests (including real YouTube search/stream extraction) passed during development. No device was connected to ADB. On-device playback, audio/video exports, codec compatibility, background transitions, SponsorBlock and PiP still require validation. Extraction success does not prove decoding/playback. Some videos, live streams, restricted content or YouTube changes can fail.

The exporter retains both a private offline copy and a public copy, increasing storage use. Removing an offline entry does not delete its public export. Muxing may take time or fail for incompatible formats. Shared downloads are not removed by clearing session data.

Source provenance, decisions, privacy scope and next validation steps: docs/BUILD_STATUS.md.
