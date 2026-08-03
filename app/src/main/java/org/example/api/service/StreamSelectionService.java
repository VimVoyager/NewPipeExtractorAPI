package org.example.api.service;

import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for intelligent stream selection before DASH manifest generation.
 * Filters and selects optimal streams to prevent overwhelming the player
 * while maintaining good quality options.
 */
@Service
public class StreamSelectionService {

    private static final Logger logger = LoggerFactory.getLogger(StreamSelectionService.class);

    private static final List<String> PREFERRED_AUDIO_CODECS = List.of(
            "mp4a",   // AAC in MP4 - broadest MSE support
            "m4a",    // MediaFormat.M4A reports this rather than mp4a
            "aac",
            "opus",   // better per-bit quality, no Safari MSE support
            "vorbis",
            "mp3"
    );

    private static final int UNRANKED_CODEC = PREFERRED_AUDIO_CODECS.size();

    private static final int UNKNOWN_BITRATE =  -1;

    private static final Pattern RESOLUTION_HEIGHT = Pattern.compile("^(\\d{3,4})(?=p|$)");

    private static final int MIN_VIDEO_QUALITIES = 3;

    private static final int MAX_VIDEO_QUALITIES = 6;

    private static final List<String> PREFERRED_SUBTITLE_FORMATS = List.of(
            "vtt", "srv3", "srv2", "srv1", "ttml"
    );

    /** Codec first, then bitrate descending. */
    private static final Comparator<AudioStream> AUDIO_PREFERENCE =
            Comparator.<AudioStream>comparingInt(StreamSelectionService::audioCodecRank)
                    .thenComparing(Comparator.<AudioStream>comparingInt(StreamSelectionService::bitrateOf).reversed());

    /**
     * Select optimal video streams for DASH manifest
     * Returns streams across multiple quality levels for adaptive streaming
     */
    public List<VideoStream> selectVideoStreams(List<VideoStream> allVideoStreams) {
        if (allVideoStreams == null || allVideoStreams.isEmpty()) {
            logger.warn("No video streams available for selection");
            return Collections.emptyList();
        }

        List<VideoStream> candidates = filterProgressiveHttp(allVideoStreams, "video");
        if (candidates.isEmpty()) {
            logger.warn("No progressive HTTP video streams available for selection");
            return Collections.emptyList();
        }

        logger.info("Selecting from {} usable video streams", candidates.size());

        // One stream per distinct height, keeping the highest bitrate at each
        Map<Integer, VideoStream> bestByHeight = new HashMap<>();
        for (VideoStream stream : candidates) {
            int height = heightOf(stream);
            if (height <= 0) {
                logger.debug("Video stream {} has no usable height (resolution '{}'); left to the bitrate fallback",
                        stream.getId(), stream.getResolution());
                continue;
            }
            bestByHeight.merge(height, stream,
                    (existing, other) -> other.getBitrate() > existing.getBitrate() ? other : existing);
        }

        List<VideoStream> selectedStreams = bestByHeight.entrySet().stream()
                .sorted(Map.Entry.<Integer, VideoStream>comparingByKey().reversed())
                .map(Map.Entry::getValue)
                .limit(MAX_VIDEO_QUALITIES)
                .collect(Collectors.toCollection(ArrayList::new));

        // Top up with the highest-bitrate leftovers if we are under the minimum
        if (selectedStreams.size() < MIN_VIDEO_QUALITIES) {
            candidates.stream()
                    .filter(s -> selectedStreams.stream().noneMatch(selected -> selected == s))
                    .sorted(Comparator.comparingInt(VideoStream::getBitrate).reversed())
                    .limit(MIN_VIDEO_QUALITIES - selectedStreams.size())
                    .forEach(selectedStreams::add);

            selectedStreams.sort(Comparator.comparingInt(StreamSelectionService::heightOf).reversed());
        }

        logger.info("Selected {} video streams from {} available",
                selectedStreams.size(), allVideoStreams.size());
        return selectedStreams;
    }

    /**
     * Numeric height for a video stream, preferring ItagItem metadata and
     * falling back to parsing the resolution label. Returns 0 when unknown.
     */
    private static int heightOf(VideoStream stream) {
        if (stream.getItagItem() != null && stream.getItagItem().getHeight() > 0) {
            return stream.getItagItem().getHeight();
        }

        String resolution = stream.getResolution();
        if (resolution == null) {
            return 0;
        }

        Matcher matcher = RESOLUTION_HEIGHT.matcher(resolution.trim());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private <T extends Stream> List<T> filterProgressiveHttp(List<T> streams, String kind) {
        List<T> progressive = new ArrayList<>();

        for (T stream : streams) {
            if (stream.getDeliveryMethod() == DeliveryMethod.PROGRESSIVE_HTTP) {
                progressive.add(stream);
            } else {
                logger.warn("Skipping {} stream {} with unsupported delivery method {}",
                        kind, stream.getId(), stream.getDeliveryMethod());
            }
        }

        return progressive;
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

        List<AudioStream> candidates = filterProgressiveHttp(allAudioStreams, "audio");
        if (candidates.isEmpty()) {
            logger.warn("No progressive HTTP audio streams available for selection");
            return Collections.emptyList();
        }

        logger.info("Selecting from {} usable audio streams", candidates.size());

        Map<String, List<AudioStream>> languageGroups = groupAudioStreamsByLanguage(candidates);

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
     * Extracts language code from a subtitle stream
     */
    private String extractLanguage(SubtitlesStream subtitle) {
        return normalizeLanguageCode(
                subtitle.getLocale() != null ? subtitle.getLocale().toLanguageTag(): "und"
        );
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
     * Select the best stream from a group of same-language streams.
     * Codec preference wins; bitrate breaks ties.
     */
    private AudioStream selectBestStreamForLanguage(List<AudioStream> streams) {
        return streams.stream()
                .min(AUDIO_PREFERENCE)
                .orElse(null);
    }

    /** Position in PREFERRED_AUDIO_CODECS; unknown codecs sort last. */
    private static int audioCodecRank(AudioStream stream) {
        String codec = audioCodecOf(stream);
        if (codec == null) {
            return UNRANKED_CODEC;
        }

        for (int i = 0; i < PREFERRED_AUDIO_CODECS.size(); i++) {
            if (codec.contains(PREFERRED_AUDIO_CODECS.get(i))) {
                return i;
            }
        }

        return UNRANKED_CODEC;
    }

    /** Codec identity from ItagItem when present, else from the MediaFormat. */
    private static String audioCodecOf(AudioStream stream) {
        if (stream.getItagItem() != null
                && stream.getItagItem().getCodec() != null
                && !stream.getItagItem().getCodec().isBlank()) {
            return stream.getItagItem().getCodec().toLowerCase();
        }

        if (stream.getFormat() != null) {
            return (stream.getFormat().getName() + " " + stream.getFormat().getSuffix()).toLowerCase();
        }

        return null;
    }

    private static int bitrateOf(AudioStream stream) {
        if (stream.getAverageBitrate() > 0) {
            return stream.getAverageBitrate();
        }

        return stream.getItagItem() != null ? stream.getItagItem().getBitrate() : UNKNOWN_BITRATE;
    }

    private static String formatName(Stream stream) {
        return stream.getFormat() != null ? stream.getFormat().getName() : "unknown";
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
     * Filters subtitles by preferred format, applying the preference cascade
     * independently within each language group. A language is never dropped
     * because a different language happened to match a higher-ranked format.
     */
    private List<SubtitlesStream> filterSubtitlesByFormat(List<SubtitlesStream> subtitles) {
        Map<String, List<SubtitlesStream>> languageGroups = new LinkedHashMap<>();

        for (SubtitlesStream subtitle : subtitles) {
            languageGroups
                    .computeIfAbsent(extractLanguage(subtitle), k -> new ArrayList<>())
                    .add(subtitle);
        }

        List<SubtitlesStream> selected = new ArrayList<>();
        for (Map.Entry<String, List<SubtitlesStream>> entry : languageGroups.entrySet()) {
            selected.addAll(filterGroupByFormat(entry.getKey(), entry.getValue()));
        }

        return selected;
    }

    /**
     * Applies the format preference cascade to a single language group,
     * falling back to the untouched group if no preferred format is present.
     */
    private List<SubtitlesStream> filterGroupByFormat(String language, List<SubtitlesStream> group) {
        for (String format : PREFERRED_SUBTITLE_FORMATS) {
            List<SubtitlesStream> filtered = group.stream()
                    .filter(sub -> matchesFormat(sub, format))
                    .collect(Collectors.toList());

            if (!filtered.isEmpty()) {
                return filtered;
            }
        }

        logger.debug("No preferred subtitle format for language '{}', keeping all {} track(s)",
                language, group.size());
        return group;
    }

    private boolean matchesFormat(SubtitlesStream subtitle, String format) {
        return subtitle.getFormat() != null
                && (format.equalsIgnoreCase(subtitle.getFormat().getName())
                || format.equalsIgnoreCase(subtitle.getFormat().getSuffix()));
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
        if (videoStreams == null || videoStreams.isEmpty()) {
            logger.warn("No suitable video streams found");
        } else {
            logger.info("Selected {} video streams:", videoStreams.size());
            for (int i = 0; i < videoStreams.size(); i++) {
                VideoStream stream = videoStreams.get(i);
                logger.info("  {}. {} - {} - {} bps",
                        i + 1, stream.getResolution(), formatName(stream), stream.getBitrate());
            }
        }

        if (audioStreams == null || audioStreams.isEmpty()) {
            logger.warn("No suitable audio streams found");
        } else {
            logger.info("Selected {} audio streams:", audioStreams.size());
            for (int i = 0; i < audioStreams.size(); i++) {
                AudioStream stream = audioStreams.get(i);
                String languageName = stream.getAudioTrackName() != null
                        ? stream.getAudioTrackName()
                        : "Unknown";
                logger.info("  {}. {} ({}) - {} - {} bps",
                        i + 1, languageName, extractLanguage(stream), formatName(stream), bitrateOf(stream));
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
            String lang = extractLanguage(subtitle);
            String displayName = subtitle.getDisplayLanguageName() != null
                    ? subtitle.getDisplayLanguageName()
                    : lang.toUpperCase();
            logger.info("  {}. {} ({}) - {} [{}]",
                    i + 1, displayName, lang, formatName(subtitle), type);
        }
    }
}