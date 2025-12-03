package org.example.api.service;

import org.example.api.dto.dash.AudioStreamMetadataDTO;
import org.example.api.dto.dash.DashManifestConfigDTO;
import org.example.api.dto.dash.SubtitleMetadataDTO;
import org.example.api.dto.dash.VideoStreamMetadataDTO;
import org.example.api.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DashManifestGeneratorService.
 * Tests XML generation, stream grouping, sorting, and manifest structure.
 */
@DisplayName("DashManifestGeneratorService Tests")
class DashManifestGeneratorServiceTest {

    private DashManifestGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new DashManifestGeneratorService();
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception for null config")
        void testGenerateManifestXml_NullConfig() {
            assertThrows(ValidationException.class, () ->
                    service.generateManifestXml(null)
            );
        }

        @Test
        @DisplayName("Should throw exception for zero duration")
        void testGenerateManifestXml_ZeroDuration() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setDurationSeconds(0);

            assertThrows(ValidationException.class, () ->
                    service.generateManifestXml(config)
            );
        }

        @Test
        @DisplayName("Should throw exception for negative duration")
        void testGenerateManifestXml_NegativeDuration() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setDurationSeconds(-100);

            assertThrows(ValidationException.class, () ->
                    service.generateManifestXml(config)
            );
        }

        @Test
        @DisplayName("Should throw exception for null stream lists")
        void testGenerateManifestXml_NullStreamLists() {
            DashManifestConfigDTO config = new DashManifestConfigDTO();
            config.setDurationSeconds(120);
            config.setMediaPresentationDuration("PT2M");
            config.setVideoStreams(null);

            assertThrows(ValidationException.class, () ->
                    service.generateManifestXml(config)
            );
        }

        @Test
        @DisplayName("Should accept empty stream lists")
        void testGenerateManifestXml_EmptyStreamLists() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(new ArrayList<>());
            config.setAudioStreams(new ArrayList<>());
            config.setSubtitleStreams(new ArrayList<>());

            assertDoesNotThrow(() -> service.generateManifestXml(config));
        }
    }

    @Nested
    @DisplayName("MPD Header Tests")
    class MpdHeaderTests {

        @Test
        @DisplayName("Should include XML declaration")
        void testGenerateManifest_XmlDeclaration() {
            DashManifestConfigDTO config = createBasicConfig();

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        }

        @Test
        @DisplayName("Should include DASH namespace")
        void testGenerateManifest_DashNamespace() {
            DashManifestConfigDTO config = createBasicConfig();

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("xmlns=\"urn:mpeg:dash:schema:mpd:2011\""));
        }

        @Test
        @DisplayName("Should include manifest type")
        void testGenerateManifest_Type() {
            DashManifestConfigDTO config = createBasicConfig();

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("type=\"static\""));
        }

        @Test
        @DisplayName("Should include media presentation duration")
        void testGenerateManifest_MediaPresentationDuration() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setMediaPresentationDuration("PT1M59.702S");

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("mediaPresentationDuration=\"PT1M59.702S\""));
        }

        @Test
        @DisplayName("Should include minBufferTime")
        void testGenerateManifest_MinBufferTime() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setMinBufferTime("PT2S");

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("minBufferTime=\"PT2S\""));
        }

        @Test
        @DisplayName("Should include profiles")
        void testGenerateManifest_Profiles() {
            DashManifestConfigDTO config = createBasicConfig();

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("profiles=\"urn:mpeg:dash:profile:isoff-on-demand:2011\""));
        }

        @Test
        @DisplayName("Should close MPD element")
        void testGenerateManifest_CloseMpd() {
            DashManifestConfigDTO config = createBasicConfig();

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.endsWith("</MPD>\n"));
        }
    }

    @Nested
    @DisplayName("Period Element Tests")
    class PeriodElementTests {

        @Test
        @DisplayName("Should include Period element")
        void testGenerateManifest_PeriodElement() {
            DashManifestConfigDTO config = createBasicConfig();

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<Period duration="));
            assertTrue(manifest.contains("</Period>"));
        }

        @Test
        @DisplayName("Should include period duration")
        void testGenerateManifest_PeriodDuration() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setMediaPresentationDuration("PT1M59S");

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<Period duration=\"PT1M59S\">"));
        }
    }

    @Nested
    @DisplayName("Video AdaptationSet Tests")
    class VideoAdaptationSetTests {

        @Test
        @DisplayName("Should not include video AdaptationSet when no video streams")
        void testGenerateManifest_NoVideoStreams() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(new ArrayList<>());

            String manifest = service.generateManifestXml(config);

            assertFalse(manifest.contains("contentType=\"video\""));
        }

        @Test
        @DisplayName("Should include video AdaptationSet with streams")
        void testGenerateManifest_WithVideoStreams() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 1920, 1080, 3000000)
            ));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("contentType=\"video\""));
            assertTrue(manifest.contains("id=\"0\""));
        }

        @Test
        @DisplayName("Should include video mimeType")
        void testGenerateManifest_VideoMimeType() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 1920, 1080, 3000000)
            ));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("mimeType=\"video/mp4\""));
        }

        @Test
        @DisplayName("Should include subsegmentAlignment for video")
        void testGenerateManifest_VideoSubsegmentAlignment() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 1920, 1080, 3000000)
            ));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("subsegmentAlignment=\"true\""));
        }

        @Test
        @DisplayName("Should include startWithSAP for video")
        void testGenerateManifest_VideoStartWithSAP() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 1920, 1080, 3000000)
            ));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("startWithSAP=\"1\""));
        }

        @Test
        @DisplayName("Should sort video streams by height descending")
        void testGenerateManifest_VideoStreamsSorted() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 640, 360, 500000),
                    createVideoStream("video-2", 1920, 1080, 3000000),
                    createVideoStream("video-3", 1280, 720, 1500000)
            ));

            String manifest = service.generateManifestXml(config);

            // 1080p should come before 720p, which should come before 360p
            int pos1080 = manifest.indexOf("height=\"1080\"");
            int pos720 = manifest.indexOf("height=\"720\"");
            int pos360 = manifest.indexOf("height=\"360\"");

            assertTrue(pos1080 > 0);
            assertTrue(pos720 > pos1080);
            assertTrue(pos360 > pos720);
        }

        @Test
        @DisplayName("Should include all video representations")
        void testGenerateManifest_MultipleVideoRepresentations() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 1920, 1080, 3000000),
                    createVideoStream("video-2", 1280, 720, 1500000),
                    createVideoStream("video-3", 640, 360, 500000)
            ));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("id=\"video-1\""));
            assertTrue(manifest.contains("id=\"video-2\""));
            assertTrue(manifest.contains("id=\"video-3\""));
            assertTrue(manifest.contains("height=\"1080\""));
            assertTrue(manifest.contains("height=\"720\""));
            assertTrue(manifest.contains("height=\"360\""));
        }
    }

    @Nested
    @DisplayName("Video Representation Tests")
    class VideoRepresentationTests {

        @Test
        @DisplayName("Should include video representation ID")
        void testVideoRepresentation_Id() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-test-123", 1920, 1080, 3000000);
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("id=\"video-test-123\""));
        }

        @Test
        @DisplayName("Should include video bandwidth")
        void testVideoRepresentation_Bandwidth() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-1", 1920, 1080, 3423702);
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("bandwidth=\"3423702\""));
        }

        @Test
        @DisplayName("Should include video codecs")
        void testVideoRepresentation_Codecs() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-1", 1920, 1080, 3000000);
            video.setCodec("avc1.640028");
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("codecs=\"avc1.640028\""));
        }

        @Test
        @DisplayName("Should include video dimensions")
        void testVideoRepresentation_Dimensions() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-1", 1920, 1080, 3000000);
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("width=\"1920\""));
            assertTrue(manifest.contains("height=\"1080\""));
        }

        @Test
        @DisplayName("Should include video frameRate")
        void testVideoRepresentation_FrameRate() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-1", 1920, 1080, 3000000);
            video.setFrameRate("24");
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("frameRate=\"24\""));
        }

        @Test
        @DisplayName("Should include BaseURL for video")
        void testVideoRepresentation_BaseUrl() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-1", 1920, 1080, 3000000);
            video.setUrl("https://example.com/video.mp4");
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<BaseURL>https://example.com/video.mp4</BaseURL>"));
        }

        @Test
        @DisplayName("Should escape XML in video URL")
        void testVideoRepresentation_EscapeUrl() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-1", 1920, 1080, 3000000);
            video.setUrl("https://example.com/video?param=1&other=2");
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("&amp;"));
            assertFalse(manifest.contains("param=1&other")); // Should be escaped
        }

        @Test
        @DisplayName("Should include SegmentBase when ranges available")
        void testVideoRepresentation_SegmentBase() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-1", 1920, 1080, 3000000);
            video.setInitRange("0-740");
            video.setIndexRange("741-1048");
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<SegmentBase indexRange=\"741-1048\">"));
            assertTrue(manifest.contains("<Initialization range=\"0-740\"/>"));
            assertTrue(manifest.contains("</SegmentBase>"));
        }

        @Test
        @DisplayName("Should not include SegmentBase when ranges missing")
        void testVideoRepresentation_NoSegmentBase() {
            DashManifestConfigDTO config = createBasicConfig();
            VideoStreamMetadataDTO video = createVideoStream("video-1", 1920, 1080, 3000000);
            video.setInitRange(null);
            video.setIndexRange(null);
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            assertFalse(manifest.contains("<SegmentBase"));
        }
    }

    // Helper methods

    private DashManifestConfigDTO createBasicConfig() {
        DashManifestConfigDTO config = new DashManifestConfigDTO();
        config.setType("static");
        config.setMediaPresentationDuration("PT2M");
        config.setMinBufferTime("PT2S");
        config.setProfiles("urn:mpeg:dash:profile:isoff-on-demand:2011");
        config.setDurationSeconds(120);
        config.setVideoStreams(new ArrayList<>());
        config.setAudioStreams(new ArrayList<>());
        config.setSubtitleStreams(new ArrayList<>());
        return config;
    }

    private VideoStreamMetadataDTO createVideoStream(String id, int width, int height, int bandwidth) {
        return VideoStreamMetadataDTO.builder()
                .id(id)
                .url("https://example.com/" + id + ".mp4")
                .codec("avc1.640028")
                .mimeType("video/mp4")
                .width(width)
                .height(height)
                .frameRate("24")
                .bandwidth(bandwidth)
                .build();
    }

    private AudioStreamMetadataDTO createAudioStream(String id, String language, int bandwidth) {
        return AudioStreamMetadataDTO.builder()
                .id(id)
                .url("https://example.com/" + id + ".mp4")
                .codec("mp4a.40.2")
                .mimeType("audio/mp4")
                .bandwidth(bandwidth)
                .audioSamplingRate("44100")
                .audioChannels(2)
                .language(language)
                .languageName(getLanguageName(language))
                .build();
    }

    private SubtitleMetadataDTO createSubtitleStream(String id, String language, String kind) {
        return SubtitleMetadataDTO.builder()
                .id(id)
                .url("https://example.com/" + id + ".vtt")
                .language(language)
                .languageName(getLanguageName(language))
                .mimeType("text/vtt")
                .kind(kind)
                .bandwidth(256)
                .build();
    }

    private String getLanguageName(String code) {
        switch (code) {
            case "en": return "English";
            case "es": return "Spanish";
            case "fr": return "French";
            case "de": return "German";
            case "und": return "Unknown";
            default: return code.toUpperCase();
        }
    }

    @Nested
    @DisplayName("Audio AdaptationSet Tests")
    class AudioAdaptationSetTests {

        @Test
        @DisplayName("Should not include audio AdaptationSets when no audio streams")
        void testGenerateManifest_NoAudioStreams() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setAudioStreams(new ArrayList<>());

            String manifest = service.generateManifestXml(config);

            assertFalse(manifest.contains("contentType=\"audio\""));
        }

        @Test
        @DisplayName("Should create separate AdaptationSet per language")
        void testGenerateManifest_AudioGroupedByLanguage() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setAudioStreams(Arrays.asList(
                    createAudioStream("audio-1", "en", 128000),
                    createAudioStream("audio-2", "en", 256000),
                    createAudioStream("audio-3", "es", 128000),
                    createAudioStream("audio-4", "es", 256000)
            ));

            String manifest = service.generateManifestXml(config);

            // Should have 2 audio AdaptationSets
            int count = countOccurrences(manifest, "contentType=\"audio\"");
            assertEquals(2, count);
        }

        @Test
        @DisplayName("Should sort languages with und first")
        void testGenerateManifest_AudioLanguageSortingUndFirst() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setAudioStreams(Arrays.asList(
                    createAudioStream("audio-1", "es", 128000),
                    createAudioStream("audio-2", "und", 128000),
                    createAudioStream("audio-3", "en", 128000)
            ));

            String manifest = service.generateManifestXml(config);

            int posUnd = manifest.indexOf("lang=\"und\"");
            int posEn = manifest.indexOf("lang=\"en\"");
            int posEs = manifest.indexOf("lang=\"es\"");

            assertTrue(posUnd < posEn);
            assertTrue(posEn < posEs);
        }

        @Test
        @DisplayName("Should include language code in AdaptationSet")
        void testGenerateManifest_AudioLanguageCode() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setAudioStreams(Arrays.asList(
                    createAudioStream("audio-1", "en", 128000)
            ));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("lang=\"en\""));
        }

        @Test
        @DisplayName("Should include language label in AdaptationSet")
        void testGenerateManifest_AudioLanguageLabel() {
            DashManifestConfigDTO config = createBasicConfig();
            AudioStreamMetadataDTO audio = createAudioStream("audio-1", "en", 128000);
            audio.setLanguageName("English");
            config.setAudioStreams(Arrays.asList(audio));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("label=\"English\""));
        }

        @Test
        @DisplayName("Should sort audio streams by bandwidth descending")
        void testGenerateManifest_AudioBandwidthSorted() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setAudioStreams(Arrays.asList(
                    createAudioStream("audio-1", "en", 64000),
                    createAudioStream("audio-2", "en", 256000),
                    createAudioStream("audio-3", "en", 128000)
            ));

            String manifest = service.generateManifestXml(config);

            int pos256 = manifest.indexOf("bandwidth=\"256000\"");
            int pos128 = manifest.indexOf("bandwidth=\"128000\"");
            int pos64 = manifest.indexOf("bandwidth=\"64000\"");

            assertTrue(pos256 < pos128);
            assertTrue(pos128 < pos64);
        }
    }

    @Nested
    @DisplayName("Audio Representation Tests")
    class AudioRepresentationTests {

        @Test
        @DisplayName("Should include audio representation ID")
        void testAudioRepresentation_Id() {
            DashManifestConfigDTO config = createBasicConfig();
            AudioStreamMetadataDTO audio = createAudioStream("audio-test-123", "en", 128000);
            config.setAudioStreams(Arrays.asList(audio));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("id=\"audio-test-123\""));
        }

        @Test
        @DisplayName("Should include audio bandwidth")
        void testAudioRepresentation_Bandwidth() {
            DashManifestConfigDTO config = createBasicConfig();
            AudioStreamMetadataDTO audio = createAudioStream("audio-1", "en", 130482);
            config.setAudioStreams(Arrays.asList(audio));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("bandwidth=\"130482\""));
        }

        @Test
        @DisplayName("Should include audio codec")
        void testAudioRepresentation_Codec() {
            DashManifestConfigDTO config = createBasicConfig();
            AudioStreamMetadataDTO audio = createAudioStream("audio-1", "en", 128000);
            audio.setCodec("mp4a.40.2");
            config.setAudioStreams(Arrays.asList(audio));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("codecs=\"mp4a.40.2\""));
        }

        @Test
        @DisplayName("Should include audio sampling rate")
        void testAudioRepresentation_SamplingRate() {
            DashManifestConfigDTO config = createBasicConfig();
            AudioStreamMetadataDTO audio = createAudioStream("audio-1", "en", 128000);
            audio.setAudioSamplingRate("44100");
            config.setAudioStreams(Arrays.asList(audio));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("audioSamplingRate=\"44100\""));
        }

        @Test
        @DisplayName("Should include AudioChannelConfiguration")
        void testAudioRepresentation_ChannelConfiguration() {
            DashManifestConfigDTO config = createBasicConfig();
            AudioStreamMetadataDTO audio = createAudioStream("audio-1", "en", 128000);
            audio.setAudioChannels(2);
            config.setAudioStreams(Arrays.asList(audio));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<AudioChannelConfiguration"));
            assertTrue(manifest.contains("schemeIdUri=\"urn:mpeg:dash:23003:3:audio_channel_configuration:2011\""));
            assertTrue(manifest.contains("value=\"2\""));
        }

        @Test
        @DisplayName("Should include BaseURL for audio")
        void testAudioRepresentation_BaseUrl() {
            DashManifestConfigDTO config = createBasicConfig();
            AudioStreamMetadataDTO audio = createAudioStream("audio-1", "en", 128000);
            audio.setUrl("https://example.com/audio.mp4");
            config.setAudioStreams(Arrays.asList(audio));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<BaseURL>https://example.com/audio.mp4</BaseURL>"));
        }

        @Test
        @DisplayName("Should include SegmentBase for audio when ranges available")
        void testAudioRepresentation_SegmentBase() {
            DashManifestConfigDTO config = createBasicConfig();
            AudioStreamMetadataDTO audio = createAudioStream("audio-1", "en", 128000);
            audio.setInitRange("0-722");
            audio.setIndexRange("723-898");
            config.setAudioStreams(Arrays.asList(audio));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<SegmentBase indexRange=\"723-898\">"));
            assertTrue(manifest.contains("<Initialization range=\"0-722\"/>"));
        }
    }

    @Nested
    @DisplayName("Subtitle AdaptationSet Tests")
    class SubtitleAdaptationSetTests {

        @Test
        @DisplayName("Should not include subtitle AdaptationSets when no subtitles")
        void testGenerateManifest_NoSubtitles() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setSubtitleStreams(new ArrayList<>());

            String manifest = service.generateManifestXml(config);

            assertFalse(manifest.contains("contentType=\"text\""));
        }

        @Test
        @DisplayName("Should create separate AdaptationSet per subtitle")
        void testGenerateManifest_SubtitleAdaptationSets() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setSubtitleStreams(Arrays.asList(
                    createSubtitleStream("subtitle-1", "en", "subtitles"),
                    createSubtitleStream("subtitle-2", "es", "subtitles"),
                    createSubtitleStream("subtitle-3", "fr", "subtitles")
            ));

            String manifest = service.generateManifestXml(config);

            int count = countOccurrences(manifest, "contentType=\"text\"");
            assertEquals(3, count);
        }

        @Test
        @DisplayName("Should include subtitle language")
        void testGenerateManifest_SubtitleLanguage() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setSubtitleStreams(Arrays.asList(
                    createSubtitleStream("subtitle-1", "en", "subtitles")
            ));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("lang=\"en\""));
        }

        @Test
        @DisplayName("Should include subtitle mimeType")
        void testGenerateManifest_SubtitleMimeType() {
            DashManifestConfigDTO config = createBasicConfig();
            SubtitleMetadataDTO subtitle = createSubtitleStream("subtitle-1", "en", "subtitles");
            subtitle.setMimeType("text/vtt");
            config.setSubtitleStreams(Arrays.asList(subtitle));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("mimeType=\"text/vtt\""));
        }

        @Test
        @DisplayName("Should include Role element for subtitles")
        void testGenerateManifest_SubtitleRole() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setSubtitleStreams(Arrays.asList(
                    createSubtitleStream("subtitle-1", "en", "subtitles")
            ));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<Role schemeIdUri=\"urn:mpeg:dash:role:2011\" value=\"subtitles\"/>"));
        }

        @Test
        @DisplayName("Should mark auto-generated subtitles correctly")
        void testGenerateManifest_AutoGeneratedSubtitles() {
            DashManifestConfigDTO config = createBasicConfig();
            SubtitleMetadataDTO subtitle = createSubtitleStream("subtitle-1", "en", "asr");
            config.setSubtitleStreams(Arrays.asList(subtitle));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("value=\"subtitles\"/>")); // asr converts to subtitles
        }

        @Test
        @DisplayName("Should include subtitle BaseURL")
        void testGenerateManifest_SubtitleBaseUrl() {
            DashManifestConfigDTO config = createBasicConfig();
            SubtitleMetadataDTO subtitle = createSubtitleStream("subtitle-1", "en", "subtitles");
            subtitle.setUrl("https://example.com/subtitle.vtt");
            config.setSubtitleStreams(Arrays.asList(subtitle));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("<BaseURL>https://example.com/subtitle.vtt</BaseURL>"));
        }

        @Test
        @DisplayName("Should include subtitle bandwidth")
        void testGenerateManifest_SubtitleBandwidth() {
            DashManifestConfigDTO config = createBasicConfig();
            SubtitleMetadataDTO subtitle = createSubtitleStream("subtitle-1", "en", "subtitles");
            subtitle.setBandwidth(256);
            config.setSubtitleStreams(Arrays.asList(subtitle));

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("bandwidth=\"256\""));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should generate complete manifest with all stream types")
        void testGenerateManifest_CompleteManifest() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setMediaPresentationDuration("PT1M59.702S");

            // Add video streams
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 1920, 1080, 3423702),
                    createVideoStream("video-2", 1280, 720, 1215044),
                    createVideoStream("video-3", 640, 360, 467558)
            ));

            // Add audio streams
            config.setAudioStreams(Arrays.asList(
                    createAudioStream("audio-1", "en", 130482),
                    createAudioStream("audio-2", "es", 128000)
            ));

            // Add subtitles
            config.setSubtitleStreams(Arrays.asList(
                    createSubtitleStream("subtitle-1", "en", "subtitles"),
                    createSubtitleStream("subtitle-2", "es", "subtitles")
            ));

            String manifest = service.generateManifestXml(config);

            // Verify structure
            assertTrue(manifest.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
            assertTrue(manifest.contains("<MPD xmlns="));
            assertTrue(manifest.contains("<Period duration="));
            assertTrue(manifest.contains("</Period>"));
            assertTrue(manifest.contains("</MPD>"));

            // Verify all streams present
            assertTrue(manifest.contains("contentType=\"video\""));
            assertTrue(manifest.contains("contentType=\"audio\""));
            assertTrue(manifest.contains("contentType=\"text\""));
        }

        @Test
        @DisplayName("Should handle manifest with only video")
        void testGenerateManifest_OnlyVideo() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 1920, 1080, 3000000)
            ));
            config.setAudioStreams(new ArrayList<>());
            config.setSubtitleStreams(new ArrayList<>());

            String manifest = service.generateManifestXml(config);

            assertTrue(manifest.contains("contentType=\"video\""));
            assertFalse(manifest.contains("contentType=\"audio\""));
            assertFalse(manifest.contains("contentType=\"text\""));
        }

        @Test
        @DisplayName("Should handle manifest with only audio")
        void testGenerateManifest_OnlyAudio() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(new ArrayList<>());
            config.setAudioStreams(Arrays.asList(
                    createAudioStream("audio-1", "en", 128000)
            ));
            config.setSubtitleStreams(new ArrayList<>());

            String manifest = service.generateManifestXml(config);

            assertFalse(manifest.contains("contentType=\"video\""));
            assertTrue(manifest.contains("contentType=\"audio\""));
            assertFalse(manifest.contains("contentType=\"text\""));
        }

        @Test
        @DisplayName("Should properly indent XML")
        void testGenerateManifest_ProperIndentation() {
            DashManifestConfigDTO config = createBasicConfig();
            config.setVideoStreams(Arrays.asList(
                    createVideoStream("video-1", 1920, 1080, 3000000)
            ));

            String manifest = service.generateManifestXml(config);

            // Check for proper indentation patterns
            assertTrue(manifest.contains("  <Period"));
            assertTrue(manifest.contains("    <AdaptationSet"));
            assertTrue(manifest.contains("      <Representation"));
        }

        @Test
        @DisplayName("Should escape all user-provided content")
        void testGenerateManifest_XmlEscaping() {
            DashManifestConfigDTO config = createBasicConfig();

            VideoStreamMetadataDTO video = createVideoStream("video-<test>", 1920, 1080, 3000000);
            video.setUrl("https://example.com/video?a=1&b=2");
            video.setCodec("avc1.640028 & more");
            config.setVideoStreams(Arrays.asList(video));

            String manifest = service.generateManifestXml(config);

            // Should not contain unescaped special characters
            assertTrue(manifest.contains("&amp;"));
            assertTrue(manifest.contains("&lt;"));
            assertTrue(manifest.contains("&gt;"));
            assertFalse(manifest.contains("video-<test>")); // Should be escaped
        }
    }

    // Helper method for counting occurrences
    private int countOccurrences(String str, String substring) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}