package org.example.api.service;

import org.example.api.dto.dash.AudioStreamMetadataDTO;
import org.example.api.dto.dash.DashManifestConfigDTO;
import org.example.api.dto.dash.SubtitleMetadataDTO;
import org.example.api.dto.dash.VideoStreamMetadataDTO;
import org.example.api.exception.ValidationException;
import org.example.api.utils.ManifestXmlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating DASH (Dynamic Adaptive Streaming over HTTP) manifests.
 * Converts StreamInfo into complete DASH XML manifests with all available streams.
 */
@Service
public class DashManifestGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(DashManifestGeneratorService.class);

    private static final String DASH_NAMESPACE = "urn:mpeg:dash:schema:mpd:2011";
    private static final String DEFAULT_VIDEO_MIME_TYPE = "video/mp4";

    private static final int VIDEO_ADAPTATION_SET_ID_BASE = 0;
    private static final int AUDIO_ADAPTATION_SET_ID_BASE = 50;
    private static final int SUBTITLE_ADAPTATION_SET_ID_BASE = 100;

    /**
     * Generates a complete DASH manifest XML from a pre-built configuration DTO.
     *
     * @param config The DASH manifest configuration
     * @return Complete DASH manifest XML string
     */
    public String generateManifestXml(DashManifestConfigDTO config) {
        validateConfig(config);

        // XML declaration

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +

                // MPD root element
                generateMpdHeader(config) +

                // Period element
                generatePeriodElement(config) +

                // Close MPD
                "</MPD>\n";
    }

    /**
     * Validates the manifest configuration.
     *
     * @param config Configuration to validate
     * @throws ValidationException if configuration is invalid
     */
    private void validateConfig(DashManifestConfigDTO config) {
        if (config == null) {
            throw new ValidationException("Manifest configuration cannot be null");
        }

        if (config.getDurationSeconds() <= 0) {
            throw new ValidationException("Duration must be greater than 0");
        }

        if (config.getVideoStreams() == null || config.getAudioStreams() == null || config.getSubtitleStreams() == null) {
            throw new ValidationException("Stream lists cannot be null");
        }
    }

    /**
     * Generates the MPD header element with attributes.
     *
     * @param config Manifest configuration
     * @return MPD header XML string
     */
    private String generateMpdHeader(DashManifestConfigDTO config) {

        return "<MPD xmlns=\"" + DASH_NAMESPACE + "\"\n" +
                "     type=\"" + config.getType() + "\"\n" +
                "     mediaPresentationDuration=\"" + config.getMediaPresentationDuration() + "\"\n" +
                "     minBufferTime=\"" + config.getMinBufferTime() + "\"\n" +
                "     profiles=\"" + config.getProfiles() + "\">\n";
    }

    /**
     * Generates the Period element containing all AdaptationSets.
     *
     * @param config Manifest configuration
     * @return Period XML string
     */
    private String generatePeriodElement(DashManifestConfigDTO config) {
        StringBuilder period = new StringBuilder();

        period.append(ManifestXmlBuilder.indent(1))
                .append("<Period duration=\"")
                .append(config.getMediaPresentationDuration())
                .append("\">\n");

        // Video AdaptationSet (if video streams exist)
        if (!config.getVideoStreams().isEmpty()) {
            period.append(generateVideoAdaptationSets(config.getVideoStreams()));
        }

        // Audio AdaptationSets (grouped by language)
        if (!config.getAudioStreams().isEmpty()) {
            period.append(generateAudioAdaptationSets(config.getAudioStreams()));
        }

        // Subtitle AdaptationSets (one per language)
        if (!config.getSubtitleStreams().isEmpty()) {
            period.append(generateSubtitleAdaptationSets(config.getSubtitleStreams()));
        }

        period.append(ManifestXmlBuilder.indent(1))
                .append("</Period>\n");

        return period.toString();
    }

    /**
     * Generates the video AdaptationSets, one per codec family.
     *
     * @param videoStreams List of all video streams
     * @return Video AdaptationSets XML string
     */
    private String generateVideoAdaptationSets(List<VideoStreamMetadataDTO> videoStreams) {
        StringBuilder xml = new StringBuilder();

        List<VideoStreamMetadataDTO> rangeBacked = videoStreams.stream()
                .filter(video -> hasByteRanges(video.getInitRange(), video.getIndexRange()))
                .toList();

        int dropped = videoStreams.size() - rangeBacked.size();
        if (dropped > 0) {
            logger.debug("Dropped {} video stream(s) without byte ranges", dropped);
        }

        List<VideoStreamMetadataDTO> usable = rangeBacked;
        if (rangeBacked.isEmpty()) {
            logger.warn("No video streams carry byte ranges; emitting all {} without SegmentBase",
                    videoStreams.size());
            usable = videoStreams;
        }

        Map<String, List<VideoStreamMetadataDTO>> streamsByCodec = usable.stream()
                .collect(Collectors.groupingBy(
                        this::videoAdaptationSetKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Highest-resolution group first; key as a stable tie-break
        List<String> sortedKeys = streamsByCodec.keySet().stream()
                .sorted(Comparator
                        .comparingInt((String key) -> maxHeight(streamsByCodec.get(key)))
                        .reversed()
                        .thenComparing(Comparator.naturalOrder()))
                .toList();

        int adaptationSetId = VIDEO_ADAPTATION_SET_ID_BASE;
        for (String key : sortedKeys) {
            xml.append(generateVideoAdaptationSet(streamsByCodec.get(key), adaptationSetId));
            adaptationSetId++;
        }

        logger.debug("Generated {} video AdaptationSet(s) from {} stream(s)",
                sortedKeys.size(), videoStreams.size());

        return xml.toString();
    }

    private String videoAdaptationSetKey(VideoStreamMetadataDTO video) {
        String codec = video.getCodec();
        String codecFamily = (codec == null || codec.isBlank())
                ? "unknown"
                : codec.trim().split("\\.")[0].toLowerCase();

        return resolveVideoMimeType(video) + "|" + codecFamily;
    }

    private String resolveVideoMimeType(VideoStreamMetadataDTO video) {
        return (video.getMimeType() != null && !video.getMimeType().isBlank())
                ? video.getMimeType()
                : DEFAULT_VIDEO_MIME_TYPE;
    }

    private int maxHeight(List<VideoStreamMetadataDTO> streams) {
        return streams.stream()
                .mapToInt(VideoStreamMetadataDTO::getHeight)
                .max()
                .orElse(0);
    }

    private boolean hasByteRanges(String initRange, String indexRange) {
        return initRange != null && !initRange.isBlank()
                && indexRange != null && !indexRange.isBlank();
    }

    /**
     * Generates a single video AdaptationSet for one codec family.
     *
     * @param videoStreams Video streams sharing a mimeType and codec family
     * @param adaptationSetId ID for this AdaptationSet
     * @return Video AdaptationSet XML string
     */
    private String generateVideoAdaptationSet(List<VideoStreamMetadataDTO> videoStreams,
                                              int adaptationSetId) {
        StringBuilder xml = new StringBuilder();

        // Sort by quality (highest first)
        List<VideoStreamMetadataDTO> sorted = videoStreams.stream()
                .sorted(Comparator.comparingInt(VideoStreamMetadataDTO::getHeight).reversed())
                .toList();

        // Every stream in this group shares the key, so any member gives the mimeType
        String mimeType = resolveVideoMimeType(sorted.getFirst());

        xml.append(ManifestXmlBuilder.indent(2))
                .append("<AdaptationSet\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("id=\"").append(adaptationSetId).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("contentType=\"video\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("mimeType=\"").append(mimeType).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("subsegmentAlignment=\"true\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("startWithSAP=\"1\">\n");

        for (VideoStreamMetadataDTO video : sorted) {
            xml.append(generateVideoRepresentation(video));
        }

        xml.append(ManifestXmlBuilder.indent(2))
                .append("</AdaptationSet>\n");

        return xml.toString();
    }

    /**
     * Generates a single video Representation element.
     *
     * @param video Video stream metadata
     * @return Representation XML string
     */
    private String generateVideoRepresentation(VideoStreamMetadataDTO video) {
        StringBuilder xml = new StringBuilder();

        xml.append(ManifestXmlBuilder.indent(3))
                .append("<Representation\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("id=\"").append(ManifestXmlBuilder.escapeXml(video.getId())).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("bandwidth=\"").append(video.getBandwidth()).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("codecs=\"").append(ManifestXmlBuilder.escapeXml(video.getCodec())).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("width=\"").append(video.getWidth()).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("height=\"").append(video.getHeight()).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("frameRate=\"").append(video.getFrameRate()).append("\">\n");

        // BaseURL
        xml.append(ManifestXmlBuilder.indent(4))
                .append("<BaseURL>")
                .append(ManifestXmlBuilder.escapeXml(video.getUrl()))
                .append("</BaseURL>\n");

        // SegmentBase (if ranges available)
        if (video.getInitRange() != null && video.getIndexRange() != null) {
            xml.append(generateSegmentBase(video.getInitRange(), video.getIndexRange()));
        }

        xml.append(ManifestXmlBuilder.indent(3))
                .append("</Representation>\n");

        return xml.toString();
    }

    /**
     * Generates audio AdaptationSets, one per language.
     * Each AdaptationSet contains all bitrate variants for that language.
     *
     * @param audioStreams List of all audio streams
     * @return Audio AdaptationSets XML string
     */
    private String generateAudioAdaptationSets(List<AudioStreamMetadataDTO> audioStreams) {
        StringBuilder xml = new StringBuilder();

        List<AudioStreamMetadataDTO> rangeBacked = audioStreams.stream()
                .filter(audio -> hasByteRanges(audio.getInitRange(), audio.getIndexRange()))
                .toList();

        int dropped = audioStreams.size() - rangeBacked.size();
        if (dropped > 0) {
            logger.debug("Dropped {} audio stream(s) without byte ranges", dropped);
        }

        List<AudioStreamMetadataDTO> usable = rangeBacked;
        if (rangeBacked.isEmpty()) {
            logger.warn("No audio streams carry byte ranges; emitting all {} without SegmentBase",
                    audioStreams.size());
            usable = audioStreams;
        }

        // Group by language
        Map<String, List<AudioStreamMetadataDTO>> streamsByLanguage = usable.stream()
                .collect(Collectors.groupingBy(
                        audio -> audio.getLanguage() != null ? audio.getLanguage() : "und"
                ));

        // Sort languages: "und" first, then "en", then alphabetical
        List<String> sortedLanguages = streamsByLanguage.keySet().stream()
                .sorted((a, b) -> {
                    if (a.equals("und")) return -1;
                    if (b.equals("und")) return 1;
                    if (a.equals("en")) return -1;
                    if (b.equals("en")) return 1;
                    return a.compareTo(b);
                })
                .toList();

        int adaptationSetId = AUDIO_ADAPTATION_SET_ID_BASE;
        for (String language : sortedLanguages) {
            List<AudioStreamMetadataDTO> languageStreams = streamsByLanguage.get(language);
            xml.append(generateAudioAdaptationSet(languageStreams, adaptationSetId, language));
            adaptationSetId++;
        }

        return xml.toString();
    }

    /**
     * Generates a single audio AdaptationSet for a specific language.
     *
     * @param audioStreams Audio streams for this language
     * @param adaptationSetId ID for this AdaptationSet
     * @param language Language code
     * @return Audio AdaptationSet XML string
     */
    private String generateAudioAdaptationSet(List<AudioStreamMetadataDTO> audioStreams,
                                              int adaptationSetId,
                                              String language) {
        StringBuilder xml = new StringBuilder();

        // Sort by bandwidth (highest first)
        List<AudioStreamMetadataDTO> sorted = audioStreams.stream()
                .sorted(Comparator.comparingInt(AudioStreamMetadataDTO::getBandwidth).reversed())
                .toList();

        // Get language name and mimeType
        String languageName = sorted.getFirst().getLanguageName();
        if (languageName == null) {
            languageName = ManifestXmlBuilder.getLanguageName(language);
        }

        String mimeType = sorted.stream()
                .map(AudioStreamMetadataDTO::getMimeType)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("audio/mp4");

        xml.append(ManifestXmlBuilder.indent(2))
                .append("<AdaptationSet\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("id=\"").append(adaptationSetId).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("contentType=\"audio\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("mimeType=\"").append(mimeType).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("lang=\"").append(language).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("label=\"").append(ManifestXmlBuilder.escapeXml(languageName)).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("subsegmentAlignment=\"true\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("startWithSAP=\"1\">\n");

        // Generate representations for each bitrate
        for (AudioStreamMetadataDTO audio : sorted) {
            xml.append(generateAudioRepresentation(audio));
        }

        xml.append(ManifestXmlBuilder.indent(2))
                .append("</AdaptationSet>\n");

        return xml.toString();
    }

    /**
     * Generates a single audio Representation element.
     *
     * @param audio Audio stream metadata
     * @return Representation XML string
     */
    private String generateAudioRepresentation(AudioStreamMetadataDTO audio) {
        StringBuilder xml = new StringBuilder();

        xml.append(ManifestXmlBuilder.indent(3))
                .append("<Representation\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("id=\"").append(ManifestXmlBuilder.escapeXml(audio.getId())).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("bandwidth=\"").append(audio.getBandwidth()).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("codecs=\"").append(ManifestXmlBuilder.escapeXml(audio.getCodec())).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(4))
                .append("audioSamplingRate=\"").append(audio.getAudioSamplingRate()).append("\">\n");

        // AudioChannelConfiguration
        xml.append(ManifestXmlBuilder.indent(4))
                .append("<AudioChannelConfiguration\n");
        xml.append(ManifestXmlBuilder.indent(5))
                .append("schemeIdUri=\"urn:mpeg:dash:23003:3:audio_channel_configuration:2011\"\n");
        xml.append(ManifestXmlBuilder.indent(5))
                .append("value=\"").append(audio.getAudioChannels()).append("\"/>\n");

        // BaseURL
        xml.append(ManifestXmlBuilder.indent(4))
                .append("<BaseURL>")
                .append(ManifestXmlBuilder.escapeXml(audio.getUrl()))
                .append("</BaseURL>\n");

        // SegmentBase (if ranges available)
        if (audio.getInitRange() != null && audio.getIndexRange() != null) {
            xml.append(generateSegmentBase(audio.getInitRange(), audio.getIndexRange()));
        }

        xml.append(ManifestXmlBuilder.indent(3))
                .append("</Representation>\n");

        return xml.toString();
    }

    /**
     * Generates subtitle AdaptationSets, one per language.
     *
     * @param subtitleStreams List of all subtitle streams
     * @return Subtitle AdaptationSets XML string
     */
    private String generateSubtitleAdaptationSets(List<SubtitleMetadataDTO> subtitleStreams) {
        StringBuilder xml = new StringBuilder();

        // Group by language
        Map<String, List<SubtitleMetadataDTO>> streamsByLanguage = subtitleStreams.stream()
                .collect(Collectors.groupingBy(
                        sub -> sub.getLanguage() != null ? sub.getLanguage() : "und"
                ));

        // Sort languages alphabetically
        List<String> sortedLanguages = streamsByLanguage.keySet().stream()
                .sorted()
                .toList();

        int adaptationSetId = SUBTITLE_ADAPTATION_SET_ID_BASE;
        for (String language : sortedLanguages) {
            List<SubtitleMetadataDTO> languageSubtitles = streamsByLanguage.get(language);

            for (SubtitleMetadataDTO subtitle : languageSubtitles) {
                xml.append(generateSubtitleAdaptationSet(subtitle, adaptationSetId));
                adaptationSetId++;
            }
        }

        return xml.toString();
    }

    /**
     * Generates a single subtitle AdaptationSet.
     *
     * @param subtitle Subtitle stream metadata
     * @param adaptationSetId ID for this AdaptationSet
     * @return Subtitle AdaptationSet XML string
     */
    private String generateSubtitleAdaptationSet(SubtitleMetadataDTO subtitle, int adaptationSetId) {
        StringBuilder xml = new StringBuilder();

        String languageName = subtitle.getLanguageName();
        if (languageName == null) {
            languageName = ManifestXmlBuilder.getLanguageName(subtitle.getLanguage());
        }

        xml.append(ManifestXmlBuilder.indent(2))
                .append("<AdaptationSet\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("id=\"").append(adaptationSetId).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("contentType=\"text\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("lang=\"").append(subtitle.getLanguage()).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("mimeType=\"").append(subtitle.getMimeType()).append("\">\n");

        // Role element (subtitles or captions)
        String role = "asr".equals(subtitle.getKind()) ? "subtitles" : subtitle.getKind();
        xml.append(ManifestXmlBuilder.indent(3))
                .append("<Role schemeIdUri=\"urn:mpeg:dash:role:2011\" value=\"")
                .append(role)
                .append("\"/>\n");

        // Representation
        xml.append(ManifestXmlBuilder.indent(3))
                .append("<Representation id=\"")
                .append(ManifestXmlBuilder.escapeXml(subtitle.getId()))
                .append("\" bandwidth=\"")
                .append(subtitle.getBandwidth())
                .append("\">\n");

        // BaseURL
        xml.append(ManifestXmlBuilder.indent(4))
                .append("<BaseURL>")
                .append(ManifestXmlBuilder.escapeXml(subtitle.getUrl()))
                .append("</BaseURL>\n");

        xml.append(ManifestXmlBuilder.indent(3))
                .append("</Representation>\n");

        xml.append(ManifestXmlBuilder.indent(2))
                .append("</AdaptationSet>\n");

        return xml.toString();
    }

    /**
     * Generates SegmentBase element with initialization and index ranges.
     *
     * @param initRange Initialization range (e.g., "0-740")
     * @param indexRange Index range (e.g., "741-1048")
     * @return SegmentBase XML string
     */
    private String generateSegmentBase(String initRange, String indexRange) {
        int indentLevel = 4;

        return ManifestXmlBuilder.indent(indentLevel) +
                "<SegmentBase indexRange=\"" +
                indexRange +
                "\">\n" +
                ManifestXmlBuilder.indent(indentLevel + 1) +
                "<Initialization range=\"" +
                initRange +
                "\"/>\n" +
                ManifestXmlBuilder.indent(indentLevel) +
                "</SegmentBase>\n";
    }
}