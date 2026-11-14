# PrivateStream workspace guidance

This is a GPL-3.0-or-later LibreTube fork. Preserve upstream notices and attribution. Source provenance and verified/unverified behavior are recorded in docs/BUILD_STATUS.md.

Build and run tests with ./build-private.ps1. Use -LiveExtraction only when network access is available; it makes real YouTube requests. Avoid changing the repaired REA MCP connection. Use REA only for shipped artifact/runtime questions; source analysis uses ordinary repository tools.

Preserve the six privacy defaults and local durable subscriptions/playlists/downloads. Background restrictions must keep user-started media and downloads working. Never describe extraction/JVM tests as device playback verification. Store release deliverables in the parent outputs directory; toolchains and temporary logs belong in the sibling work directory.
