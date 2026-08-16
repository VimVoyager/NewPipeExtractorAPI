# Changelog

All notable changes to this service are documented here.

The format is based on [keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). See [VERSIONING.md](VERSIONING.md) for how versions are chosen and released.

## [Unreleased]

## [0.2.0] - 2026-08-16

### Added

 - `StreamInfo` caching keyed on video URL. Repeated requests for the same video now share one extraction instead of re-extracting per endpoint.
 - Configurable cache settings under `opentube.cache.streaminfo`: `max-entries`, `safety-margin`, `fallback-ttl`, `degraded-ttl`, `max-ttl`
 - Request scoped logging context: every log line has a request ID and a resource ID from the request to better identify logs from concurrent requests.
 - `local` Spring profile (`application-local.properties`) enabling DEBUG for the application package during local development.

### Changed

 - Kiosk endpoints moved out of `NewPipeController`/`RestService` into a dedicated `KioskController` and `KioskService`
 - Kiosk responses now return typed DTOs with ISO-8601 timestamps instead of serialised extractor objects
 - `/api/v1/streams/dash` now returns 503 with the standard error body and a `NO_STREAMS_AVAILABLE` code when no streams are found.
 - Log levels overhauled across all services and controllers: entry lines moved to DEBUG, one INFO line per completed operation

### Removed
 
 - Kiosk pagination endpoint (`GET /api/v1/kiosks/{kioskId}/page`) - YouTube kiosk feeds return no continuation token and are single-page by design

### Fixed

 - Granular stream endpoints `/audio`, `/video`, `/subtitles`, `/thumbnails`, `/segments`, `/preview-frames`, `/description`, `/related`) bypassed the incomplete-response retry, so they could return empty or muxed-only results where `/dash` succeeded for the same video.
 - `PlaylistService` logged an error on every successful playlist page request.

## [0.1.1] - 2026-08-09

### Changed

 - Each content type now has its own `AdaptationSet` id range, keeping ids unique within a `Period` now that video can emit more than one set. Video starts at 0, audio at 50, subtitles at 100.

### Fixed

 - Subtitle format preference now runs within each language group rather than across the whole list, so a language is no longer dropped entirely because a different language matched a higher-ranked format.
 - Video `AdaptationSets` are split by mimeType and codec family, so the mimeType declared on a set always matches the `Representations` inside it. Mixed vp9/webm and avc1/mp4 sets previously produced a non-conformant manifest.
 - Video quality selection keys on numeric height instead of resolution labels, matching how the manifest generator sorts `Representations`. Labels such as `1080p60` and `1080p HDR` are now recognised.
 - Audio track preference is driven by codec metadata rather than hardcoded YouTube itag numbers, which are an implementation detail that can change without notice.
 - Streams that are not delivered over progressive HTTP, and streams without both an init and index byte range, are filtered out with a log line rather than being emitted as unplayable `Representations`.
 - Replaced `assert` statements in stream selection and logging with real null checks. Assertions are disabled at runtime unless `-ea` is set, so they provided no protection in production.

[Unreleased]: https://github.com/VimVoyager/NewPipeExtractorApi/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/VimVoyager/NewPipeExtractorApi/compare/v1.1.1...v0.2.0
[0.1.1]: https://github.com/VimVoyager/NewPipeExtractorApi/compare/v0.1.0...v0.1.1