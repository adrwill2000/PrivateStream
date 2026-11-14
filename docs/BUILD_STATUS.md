# Build and implementation record

Date: 2026-11-14 (America/Denver).

## Provenance and base selection

LibreTube source: https://github.com/libre-tube/LibreTube
Exact upstream revision: 223b6a2c558e42e67541381c0ecff94c90eba76f (default branch checkout, not claimed to equal release tag).
Latest release reported by GitHub at inspection: v32.1. Repository active, GPL-3.0; project README permits GPL v3 or later.
The existing source uses Kotlin and Android Views/ViewBinding/Material, not Compose; Media3 1.9.2, Room 2.8.4, and libre-tube/NewPipeExtractor revision 894ae97. No wholesale UI rewrite was needed.

Alternatives inspected using official GitHub repository/release metadata:
- TeamNewPipe/NewPipe: active, GPL-3.0; latest v0.29.3, released November 10, 2026. Useful extraction reference; switching would duplicate working LibreTube features.
- JunkFood02/Seal: GPL-3.0, latest stable metadata v1.13.1 (October 16, 2024), repository updated September 2026. Useful downloader reference, not a playback client base.
- yausername/youtubedl-android: GPL-3.0, repository last pushed November 2025; no GitHub latest release endpoint available. Not integrated. No yt-dlp/FFmpeg binaries were added: LibreTube already has a coherent downloader, SABR path and Media3 exporter. Avoided combining redundant engines and native packaging/licensing burden for this milestone.

Preserved upstream LICENSE, notices, source and assets. PrivateStream modifications are under the same GPL-3.0-or-later terms. The corresponding source ZIP accompanies the debug APK. Do not distribute only the APK without providing corresponding source and license information.

## Environment and verification

No existing PrivateStream starter/archive found in searched Documents, Downloads and OneDrive Documents locations. The old sandbox ZIP was unavailable. Existing WSL command execution returned Wsl/E_ACCESSDENIED; its repaired REA connection was not changed. No reverse engineering was necessary for this source-based work.

Local isolated tools in sibling work/: Temurin JDK 21.0.13, Android command-line SDK, platform 36, build tools, platform-tools, Gradle 9.4.1 wrapper/cache. No existing Android installation was replaced. Windows protoc 33.5 and 25.8 failed to launch with error 623; official protoc 3.20.3 runs and generates compatible Java lite sources against runtime 4.33.5. Override is local and optional.

Build command: ./build-private.ps1 -LiveExtraction
Expected checks: assembleDebug and testDebugUnitTest. Latest final result is recorded in the delivered validation log and test-results ZIP.
NetworkLedger tests cover blocking background browsing while preserving transfers/extraction, bounded storage, immutable snapshots and clearing. LiveExtractionTest uses real YouTube search and a public Big Buck Bunny video, and asserts nonempty audio/video stream extraction. Existing text, UMP and composite-buffer tests retained.

ADB device inventory was empty. No APK installation or device interaction was performed. No emulator was assumed available. Real decoder output, audio output, PiP transitions, background playback survival, SponsorBlock skips, public file validity and offline playback are unverified.

## Remaining validation and hardening

1. Install preview on the Pixel; search, play a video, seek, lock screen, test notification controls and PiP.
2. Save video/audio; inspect playable files in Download/PrivateStream and offline library. Test concurrent downloads, pause/resume, mux failure, low storage and cancellation.
3. Test SponsorBlock with known marked content, local subscriptions/playlists and offline launch.
4. Confirm background policy on real lifecycle transitions and redirects; instrument separate Media3 HTTP requests if complete dashboard coverage is needed. Playback-support requests (SponsorBlock lookup and token generation) are also permitted while those services are active. Current guard permits extraction whenever a player/download service is active, not per-request user intent.
5. Broaden lifecycle/MediaStore instrumentation tests; validate process death/export recovery. Pending MediaStore rows after abrupt process termination need recovery handling before a production release.
6. Final PrivateStream visual identity and release signing. Current preview retains credited LibreTube icons/assets. Debug signing is for development only.

## Codex project organization

This chat has a dedicated durable source checkout and Git history. The available app tools list/move existing projects but expose no project registration action; listing projects showed only ChatGPT projects. A sidebar Codex project was not registered. No claim that it was created.
