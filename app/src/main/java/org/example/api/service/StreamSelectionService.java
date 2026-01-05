package org.example.api.service;

import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for intelligent stream selection before DASH manifest generation.
 * Filters and selects optimal streams to prevent overwhelming the player
 * while maintaining good quality options.
 */
@Service
public class StreamSelectionService {

    private static final Logger logger = LoggerFactory.getLogger(StreamSelectionService.class);

    private static final List<String> PREFERRED_AUDIO_ITAGS = List.of(
            "141", // m4a 256kbps
            "140", // m4a 128kbps
            "251", // webm 160kbps
            "250", // webm 70kbps
            "249", // webm 50kbps
            "139"  // m4a 48kbps
    );

    private static final List<String> QUALITY_LEVELS = List.of(
            "2160p", // 4K
            "1440p", // 2K
            "1080p", // Full HD
            "720p",  // HD
            "480p",  // SD
            "360p",
            "240p",
            "144p"
    );

    private static final int MIN_VIDEO_QUALITIES = 3;

    private static final int MAX_VIDEO_QUALITIES = 6;

    private static final List<String> PREFERRED_SUBTITLE_FORMATS = List.of(
            "vtt", "srv3", "srv2", "srv1", "ttml"
    );

    /**
     * Select optimal video streams for DASH manifest
     * Returns streams across multiple quality levels for adaptive streaming
     */
    public List<VideoStream> selectVideoStreams(List<VideoStream> allVideoStreams) {
        if (allVideoStreams == null || allVideoStreams.isEmpty()) {
            logger.warn("No video streams available for selection");
            return Collections.emptyList();
        }

        logger.info("Selecting from {} total video streams", allVideoStreams.size());

        List<VideoStream> selectedStreams = new ArrayList<>();

        // First pass: Try to get one stream per quality level
        for (String quality : QUALITY_LEVELS) {
            Optional<VideoStream> match = allVideoStreams.stream()
                    .filter(s -> quality.equals(s.getResolution()))
                    .filter(s -> selectedStreams.stream()
                            .noneMatch(existing -> quality.equals(existing.getResolution())))
                    .findFirst();

            match.ifPresent(selectedStreams::add);

            // Stop if we have enough qualities
            if (selectedStreams.size() >= MAX_VIDEO_QUALITIES) {
                break;
            }
        }

        // Second pass: If we don't have minimum qualities, add by highest bitrate
        if (selectedStreams.size() < MIN_VIDEO_QUALITIES) {
            List<VideoStream> remainingStreams = allVideoStreams.stream()
                    .filter(s -> selectedStreams.stream()
                            .noneMatch(existing -> existing.getId().equals(s.getId())))
                    .sorted((s1, s2) -> Integer.compare(s2.getBitrate(), s1.getBitrate()))
                    .limit(MIN_VIDEO_QUALITIES - selectedStreams.size())
                    .toList();

            selectedStreams.addAll(remainingStreams);
        }

        // Sort by quality (highest first) for better player behavior
        selectedStreams.sort((a, b) -> {
            int qualityIndexA = QUALITY_LEVELS.indexOf(a.getResolution());
            int qualityIndexB = QUALITY_LEVELS.indexOf(b.getResolution());

            if (qualityIndexA == -1) qualityIndexA = Integer.MAX_VALUE;
            if (qualityIndexB == -1) qualityIndexB = Integer.MAX_VALUE;

            return Integer.compare(qualityIndexA, qualityIndexB);
        });

        logger.info("Selected {} video streams from {} available", selectedStreams.size(), allVideoStreams.size());
        return selectedStreams;
    }

    /**
     * Select best audio streams - one per available language
     * Returns array of audio streams with different languages, sorted by preference
     */
    public List<AudioStream> selectAudioStreams(List<AudioStream> allAudioStreams) {
        if (allAudioStreams == null || allAudioStreams.isEmpty()) {
            logger.warn("No audio streams available for selection");
            return Collections.emptyList();
        }

        logger.info("Selecting from {} total audio streams", allAudioStreams.size());

        // Group streams by language
        Map<String, List<AudioStream>> languageGroups = groupAudioStreamsByLanguage(allAudioStreams);

        List<AudioStream> selectedStreams = new ArrayList<>();

        // For each language, select the best stream
        for (Map.Entry<String, List<AudioStream>> entry : languageGroups.entrySet()) {
            AudioStream bestStream = selectBestStreamForLanguage(entry.getValue());
            if (bestStream != null) {
                selectedStreams.add(bestStream);
            }
        }

        // Sort by language preference (original/primary first, then English, then alphabetically)
        selectedStreams.sort(this::compareByLanguagePriority);

        logger.info("Selected {} audio streams ({} languages) from {} available",
                selectedStreams.size(), languageGroups.size(), allAudioStreams.size());
        return selectedStreams;
    }

    /**
     * Select optimal subtitles - deduplicated and filtered by format preference
     */
    public List<SubtitlesStream> selectSubtitles(List<SubtitlesStream> allSubtitles) {
        if (allSubtitles == null || allSubtitles.isEmpty()) {
            logger.info("No subtitles available for selection");
            return Collections.emptyList();
        }

        logger.info("Selecting from {} total subtitle streams", allSubtitles.size());

        // Filter by preferred formats
        List<SubtitlesStream> formatFiltered = filterSubtitlesByFormat(allSubtitles);

        // Deduplicate by language (prefer manual over auto-generated)
        List<SubtitlesStream> deduplicated = deduplicateSubtitles(formatFiltered);

        // Sort by language priority
        deduplicated.sort(this::compareSubtitlesByLanguage);

        logger.info("Selected {} subtitle streams from {} available", deduplicated.size(), allSubtitles.size());
        return deduplicated;
    }

    /**
     * Groups audio streams by language
     */
    private Map<String, List<AudioStream>> groupAudioStreamsByLanguage(List<AudioStream> streams) {
        Map<String, List<AudioStream>> languageMap = new HashMap<>();

        for (AudioStream stream : streams) {
            String language = extractLanguage(stream);
            languageMap.computeIfAbsent(language, k -> new ArrayList<>()).add(stream);
        }

        return languageMap;
    }

    /**
     * Extracts language code from audio stream
     */
    private String extractLanguage(AudioStream stream) {
        if (stream.getAudioLocale() != null) {
            return normalizeLanguageCode(stream.getAudioLocale().toLanguageTag());
        }

        if (stream.getAudioTrackId() != null && !stream.getAudioTrackId().isEmpty()) {
            return normalizeLanguageCode(stream.getAudioTrackId());
        }

        return "und"; // undefined
    }

    /**
     * Normalizes language code to standard format
     */
    private String normalizeLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return "und";
        }
        return languageCode.replace("_", "-").toLowerCase();
    }

    /**
     * Select the best stream from a group of same-language streams
     */
    private AudioStream selectBestStreamForLanguage(List<AudioStream> streams) {
        if (streams.isEmpty()) {
            return null;
        }

        // First, try preferred itag
        for (String itag : PREFERRED_AUDIO_ITAGS) {
            Optional<AudioStream> match = streams.stream()
                    .filter(s -> {
                        assert s.getItagItem() != null;
                        return String.valueOf(s.getItagItem().id).equals(itag);
                    })
                    .findFirst();
            if (match.isPresent()) {
                return match.get();
            }
        }

        // Fallback: Best M4A/AAC stream by bitrate
        Optional<AudioStream> m4aStream = streams.stream()
                .filter(s -> s.getFormat() != null &&
                        (s.getFormat().getName().equalsIgnoreCase("M4A") ||
                                s.getFormat().getName().equalsIgnoreCase("MP4A")))
                .max(Comparator.comparingInt(s -> {
                    assert s.getItagItem() != null;
                    return s.getItagItem().getBitrate();
                }));

        return m4aStream.orElseGet(() -> streams.stream()
                .max(Comparator.comparingInt(s -> {
                    assert s.getItagItem() != null;
                    return s.getItagItem().getBitrate();
                }))
                .orElse(null));

    }

    /**
     * Compare streams by language priority
     */
    private int compareByLanguagePriority(AudioStream a, AudioStream b) {
        String langA = extractLanguage(a);
        String langB = extractLanguage(b);

        int priorityA = getLanguagePriority(langA);
        int priorityB = getLanguagePriority(langB);

        if (priorityA != priorityB) {
            return Integer.compare(priorityA, priorityB);
        }

        return langA.compareTo(langB);
    }

    /**
     * Determines the priority order for sorting languages
     */
    private int getLanguagePriority(String languageCode) {
        // Highest priority: original/undefined audio
        if ("und".equals(languageCode) || "original".equals(languageCode)) {
            return 0;
        }

        // Second priority: English
        if ("en".equals(languageCode)) {
            return 1;
        }

        // All others sorted alphabetically
        return 2;
    }

    /**
     * Filters subtitles by preferred formats
     */
    private List<SubtitlesStream> filterSubtitlesByFormat(List<SubtitlesStream> subtitles) {
        // Try preferred formats in order
        for (String format : PREFERRED_SUBTITLE_FORMATS) {
            List<SubtitlesStream> filtered = subtitles.stream()
                    .filter(sub -> {
                        assert sub.getFormat() != null;
                        return format.equalsIgnoreCase(sub.getFormat().getName()) ||
                                format.equalsIgnoreCase(sub.getFormat().getSuffix());
                    })
                    .collect(Collectors.toList());

            if (!filtered.isEmpty()) {
                return filtered;
            }
        }

        // Fallback: return all if no preferred format found
        return subtitles;
    }

    /**
     * Deduplicates subtitles by language, preferring manual over auto-generated
     */
    private List<SubtitlesStream> deduplicateSubtitles(List<SubtitlesStream> subtitles) {
        Map<String, SubtitlesStream> languageMap = new HashMap<>();

        for (SubtitlesStream subtitle : subtitles) {
            String language = normalizeLanguageCode(
                    subtitle.getLocale() != null ? subtitle.getLocale().toLanguageTag() : "und"
            );

            SubtitlesStream existing = languageMap.get(language);

            // If no existing subtitle for this language, or if this one is better quality
            if (existing == null || (!subtitle.isAutoGenerated() && existing.isAutoGenerated())) {
                languageMap.put(language, subtitle);
            }
        }

        return new ArrayList<>(languageMap.values());
    }

    /**
     * Compare subtitles by language priority
     */
    private int compareSubtitlesByLanguage(SubtitlesStream a, SubtitlesStream b) {
        String langA = normalizeLanguageCode(
                a.getLocale() != null ? a.getLocale().toLanguageTag() : "und"
        );
        String langB = normalizeLanguageCode(
                b.getLocale() != null ? b.getLocale().toLanguageTag() : "und"
        );

        int priorityA = getLanguagePriority(langA);
        int priorityB = getLanguagePriority(langB);

        if (priorityA != priorityB) {
            return Integer.compare(priorityA, priorityB);
        }

        // Within same priority, prefer manual over auto-generated
        if (a.isAutoGenerated() != b.isAutoGenerated()) {
            return a.isAutoGenerated() ? 1 : -1;
        }

        return langA.compareTo(langB);
    }

    /**
     * Log selected streams for debugging
     */
    public void logSelectedStreams(List<VideoStream> videoStreams, List<AudioStream> audioStreams) {
        if (videoStreams.isEmpty()) {
            logger.warn("No suitable video streams found");
        } else {
            logger.info("Selected {} video streams:", videoStreams.size());
            for (int i = 0; i < videoStreams.size(); i++) {
                VideoStream stream = videoStreams.get(i);
                assert stream.getFormat() != null;
                assert stream.getItagItem() != null;
                logger.info("  {}. {} - {} - {} bps",
                        i + 1, stream.getResolution(), stream.getFormat().getName(),
                        stream.getItagItem().getBitrate());
            }
        }

        if (audioStreams.isEmpty()) {
            logger.warn("No suitable audio streams found");
        } else {
            logger.info("Selected {} audio streams:", audioStreams.size());
            for (int i = 0; i < audioStreams.size(); i++) {
                AudioStream stream = audioStreams.get(i);
                String language = extractLanguage(stream);
                String languageName = stream.getAudioTrackName() != null
                        ? stream.getAudioTrackName()
                        : "Unknown";
                assert stream.getFormat() != null;
                assert stream.getItagItem() != null;
                logger.info("  {}. {} ({}) - {} - {} bps",
                        i + 1, languageName, language, stream.getFormat().getName(),
                        stream.getItagItem().getBitrate());
            }
        }
    }

    /**
     * Log selected subtitles for debugging
     */
    public void logSelectedSubtitles(List<SubtitlesStream> subtitles) {
        if (subtitles.isEmpty()) {
            logger.info("No subtitles available");
            return;
        }

        logger.info("Selected {} subtitle tracks:", subtitles.size());
        for (int i = 0; i < subtitles.size(); i++) {
            SubtitlesStream subtitle = subtitles.get(i);
            String type = subtitle.isAutoGenerated() ? "auto" : "manual";
            String lang = normalizeLanguageCode(
                    subtitle.getLocale() != null ? subtitle.getLocale().toLanguageTag() : "und"
            );
            String displayName = subtitle.getDisplayLanguageName() != null
                    ? subtitle.getDisplayLanguageName()
                    : lang.toUpperCase();
            assert subtitle.getFormat() != null;
            logger.info("  {}. {} ({}) - {} [{}]",
                    i + 1, displayName, lang, subtitle.getFormat().getName(), type);
        }
    }
}