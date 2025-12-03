package org.example.api.dto.dash;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for DashManifestConfigDTO.
 * Tests validation, factory methods, builder pattern, and JSON serialization.
 */
@DisplayName("DashManifestConfigDTO Tests")
class DashManifestConfigDTOTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build DTO with all fields")
        void testBuilder_AllFields() {
            // Arrange
            List<VideoStreamMetadataDTO> videoStreams = Arrays.asList(
                    VideoStreamMetadataDTO.builder()
                            .id("video-1")
                            .url("https://example.com/video.mp4")
                            .codec("avc1.640028")
                            .mimeType("video/mp4")
                            .width(1920)
                            .height(1080)
                            .frameRate("24")
                            .bandwidth(3423702)
                            .build()
            );

            List<AudioStreamMetadataDTO> audioStreams = Arrays.asList(
                    AudioStreamMetadataDTO.builder()
                            .id("audio-1")
                            .url("https://example.com/audio.mp4")
                            .codec("mp4a.40.2")
                            .mimeType("audio/mp4")
                            .bandwidth(130482)
                            .audioSamplingRate("44100")
                            .audioChannels(2)
                            .build()
            );

            List<SubtitleMetadataDTO> subtitleStreams = Arrays.asList(
                    SubtitleMetadataDTO.builder()
                            .id("subtitle-1")
                            .url("https://example.com/subtitle.vtt")
                            .language("en")
                            .mimeType("text/vtt")
                            .build()
            );

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.builder()
                    .type("static")
                    .mediaPresentationDuration("PT1M59.702S")
                    .minBufferTime("PT2S")
                    .profiles("urn:mpeg:dash:profile:isoff-on-demand:2011")
                    .videoStreams(videoStreams)
                    .audioStreams(audioStreams)
                    .subtitleStreams(subtitleStreams)
                    .durationSeconds(120)
                    .build();

            // Assert
            assertEquals("static", dto.getType());
            assertEquals("PT1M59.702S", dto.getMediaPresentationDuration());
            assertEquals("PT2S", dto.getMinBufferTime());
            assertEquals("urn:mpeg:dash:profile:isoff-on-demand:2011", dto.getProfiles());
            assertEquals(1, dto.getVideoStreams().size());
            assertEquals(1, dto.getAudioStreams().size());
            assertEquals(1, dto.getSubtitleStreams().size());
            assertEquals(120, dto.getDurationSeconds());
        }

        @Test
        @DisplayName("Should use default values in default constructor")
        void testDefaultConstructor() {
            // Act
            DashManifestConfigDTO dto = new DashManifestConfigDTO();

            // Assert
            assertEquals("static", dto.getType());
            assertEquals("PT2S", dto.getMinBufferTime());
            assertEquals("urn:mpeg:dash:profile:isoff-on-demand:2011", dto.getProfiles());
            assertNotNull(dto.getVideoStreams());
            assertNotNull(dto.getAudioStreams());
            assertNotNull(dto.getSubtitleStreams());
            assertTrue(dto.getVideoStreams().isEmpty());
            assertTrue(dto.getAudioStreams().isEmpty());
            assertTrue(dto.getSubtitleStreams().isEmpty());
        }

        @Test
        @DisplayName("Should handle null stream lists in constructor")
        void testConstructor_NullStreamLists() {
            // Act
            DashManifestConfigDTO dto = new DashManifestConfigDTO(
                    "static",
                    "PT1M59S",
                    "PT2S",
                    "urn:mpeg:dash:profile:isoff-on-demand:2011",
                    null,
                    null,
                    null,
                    119
            );

            // Assert
            assertNotNull(dto.getVideoStreams());
            assertNotNull(dto.getAudioStreams());
            assertNotNull(dto.getSubtitleStreams());
            assertTrue(dto.getVideoStreams().isEmpty());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation for valid DTO")
        void testValidation_ValidDto() {
            // Arrange
            DashManifestConfigDTO dto = DashManifestConfigDTO.builder()
                    .type("static")
                    .mediaPresentationDuration("PT1M59S")
                    .minBufferTime("PT2S")
                    .profiles("urn:mpeg:dash:profile:isoff-on-demand:2011")
                    .videoStreams(new ArrayList<>())
                    .audioStreams(new ArrayList<>())
                    .subtitleStreams(new ArrayList<>())
                    .durationSeconds(119)
                    .build();

            // Act
            Set<ConstraintViolation<DashManifestConfigDTO>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
        }

        @Test
        @DisplayName("Should fail validation when type is blank")
        void testValidation_BlankType() {
            // Arrange
            DashManifestConfigDTO dto = DashManifestConfigDTO.builder()
                    .type("")
                    .mediaPresentationDuration("PT1M59S")
                    .minBufferTime("PT2S")
                    .profiles("urn:mpeg:dash:profile:isoff-on-demand:2011")
                    .videoStreams(new ArrayList<>())
                    .audioStreams(new ArrayList<>())
                    .subtitleStreams(new ArrayList<>())
                    .durationSeconds(119)
                    .build();

            // Act
            Set<ConstraintViolation<DashManifestConfigDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Manifest type cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when mediaPresentationDuration is blank")
        void testValidation_BlankMediaPresentationDuration() {
            // Arrange
            DashManifestConfigDTO dto = DashManifestConfigDTO.builder()
                    .type("static")
                    .mediaPresentationDuration("")
                    .minBufferTime("PT2S")
                    .profiles("urn:mpeg:dash:profile:isoff-on-demand:2011")
                    .videoStreams(new ArrayList<>())
                    .audioStreams(new ArrayList<>())
                    .subtitleStreams(new ArrayList<>())
                    .durationSeconds(119)
                    .build();

            // Act
            Set<ConstraintViolation<DashManifestConfigDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Media presentation duration cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when durationSeconds is zero")
        void testValidation_ZeroDuration() {
            // Arrange
            DashManifestConfigDTO dto = DashManifestConfigDTO.builder()
                    .type("static")
                    .mediaPresentationDuration("PT0S")
                    .minBufferTime("PT2S")
                    .profiles("urn:mpeg:dash:profile:isoff-on-demand:2011")
                    .videoStreams(new ArrayList<>())
                    .audioStreams(new ArrayList<>())
                    .subtitleStreams(new ArrayList<>())
                    .durationSeconds(0)
                    .build();

            // Act
            Set<ConstraintViolation<DashManifestConfigDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Duration must be at least 1 second")));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from StreamInfo")
        void testFrom_ValidStreamInfo() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            VideoStream videoStream = createMockVideoStream();
            AudioStream audioStream = createMockAudioStream();
            SubtitlesStream subtitleStream = createMockSubtitlesStream();

            when(streamInfo.getDuration()).thenReturn(119L);
            when(streamInfo.getVideoOnlyStreams()).thenReturn(Arrays.asList(videoStream));
            when(streamInfo.getAudioStreams()).thenReturn(Arrays.asList(audioStream));
            when(streamInfo.getSubtitles()).thenReturn(Arrays.asList(subtitleStream));

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert
            assertEquals("static", dto.getType());
            assertEquals("PT2S", dto.getMinBufferTime());
            assertEquals(119, dto.getDurationSeconds());
            assertEquals("PT1M59S", dto.getMediaPresentationDuration());
            assertEquals(1, dto.getVideoStreams().size());
            assertEquals(1, dto.getAudioStreams().size());
            assertEquals(1, dto.getSubtitleStreams().size());
        }

        @Test
        @DisplayName("Should handle StreamInfo with empty streams")
        void testFrom_EmptyStreams() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);

            when(streamInfo.getDuration()).thenReturn(120L);
            when(streamInfo.getVideoOnlyStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getAudioStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getSubtitles()).thenReturn(new ArrayList<>());

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert
            assertTrue(dto.getVideoStreams().isEmpty());
            assertTrue(dto.getAudioStreams().isEmpty());
            assertTrue(dto.getSubtitleStreams().isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when StreamInfo is null")
        void testFrom_NullStreamInfo() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    DashManifestConfigDTO.from(null)
            );

            assertTrue(exception.getMessage().contains("StreamInfo cannot be null"));
        }

        @Test
        @DisplayName("Should handle multiple streams of each type")
        void testFrom_MultipleStreams() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            List<VideoStream> videoStreams = Arrays.asList(
                    createMockVideoStream(),
                    createMockVideoStream(),
                    createMockVideoStream()
            );
            List<AudioStream> audioStreams = Arrays.asList(
                    createMockAudioStream(),
                    createMockAudioStream()
            );
            List<SubtitlesStream> subtitleStreams = Arrays.asList(
                    createMockSubtitlesStream(),
                    createMockSubtitlesStream(),
                    createMockSubtitlesStream(),
                    createMockSubtitlesStream()
            );

            when(streamInfo.getDuration()).thenReturn(180L);
            when(streamInfo.getVideoOnlyStreams()).thenReturn(videoStreams);
            when(streamInfo.getAudioStreams()).thenReturn(audioStreams);
            when(streamInfo.getSubtitles()).thenReturn(subtitleStreams);

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert
            assertEquals(3, dto.getVideoStreams().size());
            assertEquals(2, dto.getAudioStreams().size());
            assertEquals(4, dto.getSubtitleStreams().size());
        }

        @Test
        @DisplayName("Should skip invalid streams without failing")
        void testFrom_InvalidStreamSkipped() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            VideoStream invalidStream = mock(VideoStream.class);
            VideoStream validStream = createMockVideoStream();

            // Make first stream throw exception
            when(invalidStream.getContent()).thenThrow(new RuntimeException("Invalid stream"));

            when(streamInfo.getDuration()).thenReturn(120L);
            when(streamInfo.getVideoOnlyStreams()).thenReturn(Arrays.asList(invalidStream, validStream));
            when(streamInfo.getAudioStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getSubtitles()).thenReturn(new ArrayList<>());

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert - should have skipped invalid stream
            assertEquals(1, dto.getVideoStreams().size());
        }

        private VideoStream createMockVideoStream() {
            VideoStream stream = mock(VideoStream.class);
            MediaFormat format = mock(MediaFormat.class);

            when(stream.getContent()).thenReturn("https://example.com/video.mp4");
            when(stream.getCodec()).thenReturn("avc1.640028");
            when(stream.getFormat()).thenReturn(format);
            when(format.getMimeType()).thenReturn("video/mp4");
            when(stream.getWidth()).thenReturn(1920);
            when(stream.getHeight()).thenReturn(1080);
            when(stream.getFps()).thenReturn(24);
            when(stream.getBitrate()).thenReturn(3423702);
            when(stream.getInitStart()).thenReturn(-1);
            when(stream.getIndexStart()).thenReturn(-1);

            return stream;
        }

        private AudioStream createMockAudioStream() {
            AudioStream stream = mock(AudioStream.class);
            MediaFormat format = mock(MediaFormat.class);
            var itagItem = mock(ItagItem.class);

            when(stream.getContent()).thenReturn("https://example.com/audio.mp4");
            when(stream.getCodec()).thenReturn("mp4a.40.2");
            when(stream.getFormat()).thenReturn(format);
            when(format.getMimeType()).thenReturn("audio/mp4");
            when(stream.getBitrate()).thenReturn(130482);
            when(stream.getItagItem()).thenReturn(itagItem);
            when(itagItem.getSampleRate()).thenReturn(44100);
            when(itagItem.getAudioChannels()).thenReturn(2);
            when(stream.getAudioLocale()).thenReturn(null);
            when(stream.getInitStart()).thenReturn(-1);
            when(stream.getIndexStart()).thenReturn(-1);

            return stream;
        }

        private SubtitlesStream createMockSubtitlesStream() {
            SubtitlesStream stream = mock(SubtitlesStream.class);

            when(stream.getContent()).thenReturn("https://example.com/subtitle.vtt");
            when(stream.getLocale()).thenReturn(java.util.Locale.ENGLISH);
            when(stream.getDisplayLanguageName()).thenReturn("English");
            when(stream.getFormat()).thenReturn(MediaFormat.VTT);
            when(stream.isAutoGenerated()).thenReturn(false);

            return stream;
        }
    }

    @Nested
    @DisplayName("Duration Formatting Tests")
    class DurationFormattingTests {

        @Test
        @DisplayName("Should format duration with hours, minutes, and seconds")
        void testFormatDuration_FullFormat() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getDuration()).thenReturn(7385L); // 2h 3m 5s
            when(streamInfo.getVideoOnlyStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getAudioStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getSubtitles()).thenReturn(new ArrayList<>());

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert
            assertEquals("PT2H3M5S", dto.getMediaPresentationDuration());
        }

        @Test
        @DisplayName("Should format duration with only minutes and seconds")
        void testFormatDuration_MinutesAndSeconds() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getDuration()).thenReturn(125L); // 2m 5s
            when(streamInfo.getVideoOnlyStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getAudioStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getSubtitles()).thenReturn(new ArrayList<>());

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert
            assertEquals("PT2M5S", dto.getMediaPresentationDuration());
        }

        @Test
        @DisplayName("Should format duration with only seconds")
        void testFormatDuration_OnlySeconds() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getDuration()).thenReturn(45L);
            when(streamInfo.getVideoOnlyStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getAudioStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getSubtitles()).thenReturn(new ArrayList<>());

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert
            assertEquals("PT45S", dto.getMediaPresentationDuration());
        }

        @Test
        @DisplayName("Should format zero duration")
        void testFormatDuration_Zero() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getDuration()).thenReturn(0L);
            when(streamInfo.getVideoOnlyStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getAudioStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getSubtitles()).thenReturn(new ArrayList<>());

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert
            assertEquals("PT0S", dto.getMediaPresentationDuration());
        }

        @Test
        @DisplayName("Should format duration with exact hour")
        void testFormatDuration_ExactHour() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getDuration()).thenReturn(3600L); // 1 hour
            when(streamInfo.getVideoOnlyStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getAudioStreams()).thenReturn(new ArrayList<>());
            when(streamInfo.getSubtitles()).thenReturn(new ArrayList<>());

            // Act
            DashManifestConfigDTO dto = DashManifestConfigDTO.from(streamInfo);

            // Assert
            assertEquals("PT1H", dto.getMediaPresentationDuration());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize DTO to JSON")
        void testSerialization() throws Exception {
            // Arrange
            DashManifestConfigDTO dto = DashManifestConfigDTO.builder()
                    .type("static")
                    .mediaPresentationDuration("PT1M59S")
                    .minBufferTime("PT2S")
                    .profiles("urn:mpeg:dash:profile:isoff-on-demand:2011")
                    .videoStreams(new ArrayList<>())
                    .audioStreams(new ArrayList<>())
                    .subtitleStreams(new ArrayList<>())
                    .durationSeconds(119)
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"static\""));
            assertTrue(json.contains("\"mediaPresentationDuration\":\"PT1M59S\""));
            assertTrue(json.contains("\"durationSeconds\":119"));
        }

        @Test
        @DisplayName("Should deserialize JSON to DTO")
        void testDeserialization() throws Exception {
            // Arrange
            String json = "{\"type\":\"static\",\"mediaPresentationDuration\":\"PT1M59S\"," +
                    "\"minBufferTime\":\"PT2S\"," +
                    "\"profiles\":\"urn:mpeg:dash:profile:isoff-on-demand:2011\"," +
                    "\"videoStreams\":[],\"audioStreams\":[],\"subtitleStreams\":[]," +
                    "\"durationSeconds\":119}";

            // Act
            DashManifestConfigDTO dto = objectMapper.readValue(json, DashManifestConfigDTO.class);

            // Assert
            assertEquals("static", dto.getType());
            assertEquals("PT1M59S", dto.getMediaPresentationDuration());
            assertEquals(119, dto.getDurationSeconds());
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTripSerialization() throws Exception {
            // Arrange
            DashManifestConfigDTO original = DashManifestConfigDTO.builder()
                    .type("static")
                    .mediaPresentationDuration("PT1M59S")
                    .minBufferTime("PT2S")
                    .profiles("urn:mpeg:dash:profile:isoff-on-demand:2011")
                    .videoStreams(new ArrayList<>())
                    .audioStreams(new ArrayList<>())
                    .subtitleStreams(new ArrayList<>())
                    .durationSeconds(119)
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(original);
            DashManifestConfigDTO deserialized = objectMapper.readValue(json, DashManifestConfigDTO.class);

            // Assert
            assertEquals(original.getType(), deserialized.getType());
            assertEquals(original.getMediaPresentationDuration(), deserialized.getMediaPresentationDuration());
            assertEquals(original.getDurationSeconds(), deserialized.getDurationSeconds());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should produce readable toString output")
        void testToString() {
            // Arrange
            DashManifestConfigDTO dto = DashManifestConfigDTO.builder()
                    .type("static")
                    .mediaPresentationDuration("PT1M59S")
                    .minBufferTime("PT2S")
                    .profiles("urn:mpeg:dash:profile:isoff-on-demand:2011")
                    .videoStreams(new ArrayList<>())
                    .audioStreams(new ArrayList<>())
                    .subtitleStreams(new ArrayList<>())
                    .durationSeconds(119)
                    .build();

            // Act
            String result = dto.toString();

            // Assert
            assertTrue(result.contains("static"));
            assertTrue(result.contains("PT1M59S"));
            assertTrue(result.contains("119"));
            assertTrue(result.contains("videoStreams=0"));
            assertTrue(result.contains("audioStreams=0"));
            assertTrue(result.contains("subtitleStreams=0"));
        }
    }
}