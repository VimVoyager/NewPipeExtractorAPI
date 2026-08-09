# Changelog

All notable changes to this service are documented here.

The format is based on [keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). See [VERSIONING.md](VERSIONING.md) for how versions are chosen and released.

## [Unreleased]

### Fixed

 - Subtitle format preference now runs with each language group rather than across the whole list, so a language is no longer dropped entirely because a different language matched a higher-ranked format.
 - Video `AdaptationSets` are split by mimeType and codec family, so the mimeType declared on a set always matches the `Representations` inside it. Mixed vp9/webm and avc1/mp4 sets previously produced a non-conformant manifest.
 - Video quality selection keys on numeric height instead of resolution labels, matching how the manifest generator sorts `Representations`. Labels such as `1080p60` and `1080p HDR` are now recognised.
 - Audio track preference is driven by codec metadata rather than hardcoded YouTube itag numbers, which are an implementation detail that can change without notice.
 - Streams that are not delivered over progressive HTTP, and streams without both an init and index byte range, are filtered out with a log line rather than being emitted as unplayable `Representations`.
 - Replaced `assert` statements in stream selection and logging with real null checks. Assertions are disabled at runtime unless `-ea` is set, so they provided no protection in production.

### Changed

 - Each content type now has its own `AdaptationSet` id range, keeping ids unique within a `Period` now that video can emit more than one set. Video starts at 0, audio at 50, subtitles at 100.

[Unreleased]: https://github.com/VimVoyager/NewPipeExtractorApi/compare/v0.1.1...HEAD