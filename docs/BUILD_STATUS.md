# Build and implementation record

Updated: 2026-11-14 (America/Denver).

## Source and license

PrivateStream is a personal fork of [LibreTube](https://github.com/libre-tube/LibreTube), licensed GPL-3.0-or-later. The original upstream license, notices, source, and assets are preserved. PrivateStream changes are distributed under the same license. The workspace contains the corresponding source for the APK; the output source archive excludes only Git metadata and generated build products.

The implementation base is LibreTube commit `223b6a2c558e42e67541381c0ecff94c90eba76f`. A fresh comparison with upstream `master` at `15e48bdd3ad36731d4cdd78e4c9db0eea573f384` found only translated string updates after this base, so no upstream application-code or dependency changes were omitted. LibreTube uses Kotlin with Android Views/ViewBinding/Material rather than Compose. This fork retains Media3 1.9.2, Room 2.8.4, and the pinned NewPipeExtractor dependency.

NewPipeExtractor is the integrated extraction path. Playback uses AndroidX Media3, including the upstream SABR implementation. Existing app downloads, offline library, local subscriptions/playlists, SponsorBlock, background playback, and Android PiP are retained. PrivateStream adds the privacy policy/dashboard and one-tap audio/video selection. The selector and download dialog accept URL-less SABR stream entries and exclude HLS for one-tap video. When player metadata has no downloadable candidates, a one-tap action reloads formats from the repository before enqueueing the existing downloader. Completed audio/video pairs are muxed by Media3 and exported to shared Downloads; standalone audio is copied there directly. No yt-dlp or FFmpeg binaries were added because the existing extractor, downloader, and Media3 exporter already form one compatible path.

## Privacy behavior in source

The six confirmed defaults remain in place: analytics/telemetry disabled; background browsing/API/image work restricted; update checks off; search history not persisted; no video privacy proxy; temporary session state cleared on normal exit. Durable Room subscriptions/playlists/downloads are kept separately. User-started playback and download transfers are exempt from the background restriction. The dashboard holds bounded host/purpose counters in memory and does not store URLs, queries, IDs, headers, or bodies. It only covers instrumented OkHttp traffic; Media3's separate transport is not counted.

## Build and verification

The existing Windows JDK 21, Android SDK platform 36/build tools 36.0.0, Gradle wrapper/cache, and debug signing key were reused. No toolchain was reinstalled or replaced. The build script stages this WSL checkout into a Windows scratch folder and copies only the resulting APK back. Toolchains and temporary build state remain outside this source tree.

Build command: `./build-private.ps1`. To make real YouTube requests, use `./build-private.ps1 -LiveExtraction` only when network access is available.

The standard JVM suite contains 14 tests: 13 pass and the explicitly opt-in live test is skipped. It includes checks that one-tap selection accepts URL-less SABR stream records and that the selected format/quality still maps to integrated download items. A force-run of `LiveExtractionTest` with `-PliveExtraction=true` passed against real YouTube search and a public Big Buck Bunny stream, returning audio and video streams. These extractor tests do not verify Android playback.

The debug APK was installed and launched on an Android 16 (`sdk_gphone64_x86_64`) emulator. Search returned current YouTube results, and the Media3 session reported `PLAYING` for a real Big Buck Bunny video. On a 52-second Sintel trailer, Save audio produced a shared WebM file and Save video downloaded the audio/video tracks, muxed them to MP4, and exported the result to `Download/PrivateStream` (7.71 MB). With Wi-Fi and mobile data disabled, that video appeared in the app's Downloads library and the Media3 session reported `PLAYING` when opened from the local library. The emulator is not a Pixel 9 Pro XL; these checks do not establish behavior on the user's phone.

The quick-download selection tests cover URL-less SABR formats and the integrated format/quality mapping. The longer Big Buck Bunny combined export was still running when the emulator app was replaced for a second instrumented build, so that run is not counted as a completed export. The shorter sample verified the full shared export and offline playback path. PiP transitions, lock-screen/background continuity, SponsorBlock skips, and preservation of local user data across process death have not been exercised on the Pixel.

Final artifact: `../outputs/PrivateStream-debug.apk`; matching source archive: `../outputs/PrivateStream-source.zip`. APK package `app.privatestream.debug`; version `0.1.0-preview` (versionCode 72); min SDK 26; target SDK 36; SHA-256 `c7976100b8e4978d8fdcdd5a6b10565ecafc679c0b0661dbeb9a6b18c9fdd63c`. `apksigner` verified the APK's v2 signature. REA independently found the APK's declared v2 record self-consistent; REA does not verify the signature's authenticity. It uses the existing Android debug certificate (`9dd0b910e8d6b35a0be4afc9e9ccf2724c2530e5ecb2a7b67ca38c5093b64fdc`) and is a development build, not a release-signed APK.

Non-fatal KSP warnings remain for missing indexes on several download foreign-key columns. They may cause full table scans and should be addressed before a release.

## Remaining validation

1. On the Pixel 9 Pro XL, repeat search/playback, one-tap audio/video download, shared-file export, and offline replay.
2. Validate PiP, media notification controls, and background playback while screen-locked.
3. Validate SponsorBlock with known marked content and check local subscriptions/playlists offline.
4. Exercise the privacy policy through lifecycle transitions, redirects, and a process restart; account for Media3 traffic separately if complete dashboard coverage is needed.
5. Revisit the KSP index warnings, pending MediaStore export recovery, and release signing before production distribution.

## Codex organization

This source checkout and Git history are in the dedicated PrivateStream workspace. Codex's app tools list the workspace as **PrivateStream**, but the available project tools provide no separate sidebar registration action.
