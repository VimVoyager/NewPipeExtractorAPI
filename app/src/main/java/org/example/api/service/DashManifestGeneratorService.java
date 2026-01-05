package org.example.api.service;

import org.example.api.dto.dash.AudioStreamMetadataDTO;
import org.example.api.dto.dash.DashManifestConfigDTO;
import org.example.api.dto.dash.SubtitleMetadataDTO;
import org.example.api.dto.dash.VideoStreamMetadataDTO;
import org.example.api.exception.ValidationException;
import org.example.api.utils.ManifestXmlBuilder;
import org.schabi.newpipe.extractor.stream.StreamInfo;
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
    private static final String DASH_PROFILE = "urn:mpeg:dash:profile:isoff-on-demand:2011";

    /**
     * Generates a complete DASH manifest XML from StreamInfo.
     *
     * @param streamInfo The stream information from NewPipe extractor
     * @return Complete DASH manifest XML string
     */
    public String generateManifest(StreamInfo streamInfo) {
        logger.info("Generating DASH manifest for video: {}", streamInfo.getName());

        // Convert StreamInfo to DTOs using existing adapter
        DashManifestConfigDTO config = DashManifestConfigDTO.from(streamInfo);

        // Validate configuration
        validateConfig(config);

        // Generate XML
        String manifest = generateManifestXml(config);

        logger.info("Generated DASH manifest with {} video, {} audio, {} subtitle streams",
                config.getVideoStreams().size(),
                config.getAudioStreams().size(),
                config.getSubtitleStreams().size());

        return manifest;
    }

    /**
     * Generates a complete DASH manifest XML from a pre-built configuration DTO.
     *
     * @param config The DASH manifest configuration
     * @return Complete DASH manifest XML string
     */
    public String generateManifestXml(DashManifestConfigDTO config) {
        validateConfig(config);

        StringBuilder xml = new StringBuilder();

        // XML declaration
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

        // MPD root element
        xml.append(generateMpdHeader(config));

        // Period element
        xml.append(generatePeriodElement(config));

        // Close MPD
        xml.append("</MPD>\n");

        return xml.toString();
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

        logger.debug("Configuration validated successfully");
    }

    /**
     * Generates the MPD header element with attributes.
     *
     * @param config Manifest configuration
     * @return MPD header XML string
     */
    private String generateMpdHeader(DashManifestConfigDTO config) {
        StringBuilder header = new StringBuilder();

        header.append("<MPD xmlns=\"").append(DASH_NAMESPACE).append("\"\n");
        header.append("     type=\"").append(config.getType()).append("\"\n");
        header.append("     mediaPresentationDuration=\"").append(config.getMediaPresentationDuration()).append("\"\n");
        header.append("     minBufferTime=\"").append(config.getMinBufferTime()).append("\"\n");
        header.append("     profiles=\"").append(config.getProfiles()).append("\">\n");

        return header.toString();
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
            period.append(generateVideoAdaptationSet(config.getVideoStreams()));
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
     * Generates the video AdaptationSet with all video quality representations.
     *
     * @param videoStreams List of all video streams
     * @return Video AdaptationSet XML string
     */
    private String generateVideoAdaptationSet(List<VideoStreamMetadataDTO> videoStreams) {
        StringBuilder xml = new StringBuilder();

        // Sort by quality (highest first)
        List<VideoStreamMetadataDTO> sorted = videoStreams.stream()
                .sorted(Comparator.comparingInt(VideoStreamMetadataDTO::getHeight).reversed())
                .toList();

        // Get the most common mimeType
        String mimeType = sorted.stream()
                .map(VideoStreamMetadataDTO::getMimeType)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("video/mp4");

        xml.append(ManifestXmlBuilder.indent(2))
                .append("<AdaptationSet\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("id=\"0\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("contentType=\"video\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("mimeType=\"").append(mimeType).append("\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("subsegmentAlignment=\"true\"\n");
        xml.append(ManifestXmlBuilder.indent(3))
                .append("startWithSAP=\"1\">\n");

        // Generate representations for each quality
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

        // Group by language
        Map<String, List<AudioStreamMetadataDTO>> streamsByLanguage = audioStreams.stream()
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

        int adaptationSetId = 1;
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

        int adaptationSetId = 100; // Start subtitle IDs at 100 to avoid conflicts
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
        StringBuilder xml = new StringBuilder();
        int indentLevel = 4;

        xml.append(ManifestXmlBuilder.indent(indentLevel))
                .append("<SegmentBase indexRange=\"")
                .append(indexRange)
                .append("\">\n");

        xml.append(ManifestXmlBuilder.indent(indentLevel + 1))
                .append("<Initialization range=\"")
                .append(initRange)
                .append("\"/>\n");

        xml.append(ManifestXmlBuilder.indent(indentLevel))
                .append("</SegmentBase>\n");

        return xml.toString();
    }
}