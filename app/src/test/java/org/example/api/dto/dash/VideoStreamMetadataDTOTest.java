package org.example.api.dto.dash;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.MediaFormat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for VideoStreamMetadataDTO.
 * Tests validation, factory methods, builder pattern, and JSON serialization.
 */
@DisplayName("VideoStreamMetadataDTO Tests")
class VideoStreamMetadataDTOTest {

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
            // Act
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .initRange("0-740")
                    .indexRange("741-1048")
                    .format("MPEG_4")
                    .build();

            // Assert
            assertEquals("video-1", dto.getId());
            assertEquals("https://example.com/video.mp4", dto.getUrl());
            assertEquals("avc1.640028", dto.getCodec());
            assertEquals("video/mp4", dto.getMimeType());
            assertEquals(1920, dto.getWidth());
            assertEquals(1080, dto.getHeight());
            assertEquals("24", dto.getFrameRate());
            assertEquals(3423702, dto.getBandwidth());
            assertEquals("0-740", dto.getInitRange());
            assertEquals("741-1048", dto.getIndexRange());
            assertEquals("MPEG_4", dto.getFormat());
        }

        @Test
        @DisplayName("Should build DTO with required fields only")
        void testBuilder_RequiredFieldsOnly() {
            // Act
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Assert
            assertEquals("video-1", dto.getId());
            assertNull(dto.getInitRange());
            assertNull(dto.getIndexRange());
            assertNull(dto.getFormat());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation for valid DTO")
        void testValidation_ValidDto() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
        }

        @Test
        @DisplayName("Should fail validation when id is blank")
        void testValidation_BlankId() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Video stream ID cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when url is blank")
        void testValidation_BlankUrl() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Video stream URL cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when codec is blank")
        void testValidation_BlankCodec() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Video codec cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when mimeType is blank")
        void testValidation_BlankMimeType() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Video MIME type cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when frameRate is blank")
        void testValidation_BlankFrameRate() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("")
                    .bandwidth(3423702)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Frame rate cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when width is zero")
        void testValidation_ZeroWidth() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(0)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Video width must be at least 1")));
        }

        @Test
        @DisplayName("Should fail validation when height is zero")
        void testValidation_ZeroHeight() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(0)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Video height must be at least 1")));
        }

        @Test
        @DisplayName("Should fail validation when bandwidth is zero")
        void testValidation_ZeroBandwidth() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(0)
                    .build();

            // Act
            Set<ConstraintViolation<VideoStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Bandwidth must be at least 1")));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from VideoStream")
        void testFrom_ValidVideoStream() {
            // Arrange
            VideoStream stream = mock(VideoStream.class);
            MediaFormat format = mock(MediaFormat.class);

            when(stream.getContent()).thenReturn("https://example.com/video.mp4");
            when(stream.getCodec()).thenReturn("avc1.640028");
            when(stream.getFormat()).thenReturn(format);
            when(format.getMimeType()).thenReturn("video/mp4");
            when(format.getName()).thenReturn("MPEG_4");
            when(stream.getWidth()).thenReturn(1920);
            when(stream.getHeight()).thenReturn(1080);
            when(stream.getFps()).thenReturn(24);
            when(stream.getBitrate()).thenReturn(3423702);
            when(stream.getInitStart()).thenReturn(0);
            when(stream.getInitEnd()).thenReturn(740);
            when(stream.getIndexStart()).thenReturn(741);
            when(stream.getIndexEnd()).thenReturn(1048);

            // Act
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("video-1", dto.getId());
            assertEquals("https://example.com/video.mp4", dto.getUrl());
            assertEquals("avc1.640028", dto.getCodec());
            assertEquals("video/mp4", dto.getMimeType());
            assertEquals(1920, dto.getWidth());
            assertEquals(1080, dto.getHeight());
            assertEquals("24", dto.getFrameRate());
            assertEquals(3423702, dto.getBandwidth());
            assertEquals("0-740", dto.getInitRange());
            assertEquals("741-1048", dto.getIndexRange());
            assertEquals("MPEG_4", dto.getFormat());
        }

        @Test
        @DisplayName("Should handle VideoStream with null format")
        void testFrom_NullFormat() {
            // Arrange
            VideoStream stream = mock(VideoStream.class);

            when(stream.getContent()).thenReturn("https://example.com/video.mp4");
            when(stream.getCodec()).thenReturn("avc1.640028");
            when(stream.getFormat()).thenReturn(null);
            when(stream.getWidth()).thenReturn(1920);
            when(stream.getHeight()).thenReturn(1080);
            when(stream.getFps()).thenReturn(24);
            when(stream.getBitrate()).thenReturn(3423702);
            when(stream.getInitStart()).thenReturn(-1);
            when(stream.getIndexStart()).thenReturn(-1);

            // Act
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.from(stream, 2);

            // Assert
            assertEquals("video-2", dto.getId());
            assertEquals("video/mp4", dto.getMimeType()); // Default
            assertNull(dto.getInitRange());
            assertNull(dto.getIndexRange());
            assertNull(dto.getFormat());
        }

        @Test
        @DisplayName("Should throw exception when VideoStream is null")
        void testFrom_NullVideoStream() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    VideoStreamMetadataDTO.from(null, 1)
            );

            assertTrue(exception.getMessage().contains("VideoStream cannot be null"));
        }

        @Test
        @DisplayName("Should handle missing init ranges")
        void testFrom_MissingInitRanges() {
            // Arrange
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
            when(stream.getInitEnd()).thenReturn(0);
            when(stream.getIndexStart()).thenReturn(-1);
            when(stream.getIndexEnd()).thenReturn(0);

            // Act
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.from(stream, 1);

            // Assert
            assertNull(dto.getInitRange());
            assertNull(dto.getIndexRange());
        }

        @Test
        @DisplayName("Should generate sequential IDs")
        void testFrom_SequentialIds() {
            // Arrange
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

            // Act
            VideoStreamMetadataDTO dto1 = VideoStreamMetadataDTO.from(stream, 1);
            VideoStreamMetadataDTO dto2 = VideoStreamMetadataDTO.from(stream, 2);
            VideoStreamMetadataDTO dto3 = VideoStreamMetadataDTO.from(stream, 10);

            // Assert
            assertEquals("video-1", dto1.getId());
            assertEquals("video-2", dto2.getId());
            assertEquals("video-10", dto3.getId());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize DTO to JSON")
        void testSerialization() throws Exception {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .initRange("0-740")
                    .indexRange("741-1048")
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertNotNull(json);
            assertTrue(json.contains("\"id\":\"video-1\""));
            assertTrue(json.contains("\"width\":1920"));
            assertTrue(json.contains("\"height\":1080"));
            assertTrue(json.contains("\"bandwidth\":3423702"));
        }

        @Test
        @DisplayName("Should deserialize JSON to DTO")
        void testDeserialization() throws Exception {
            // Arrange
            String json = "{\"id\":\"video-1\",\"url\":\"https://example.com/video.mp4\"," +
                    "\"codec\":\"avc1.640028\",\"mimeType\":\"video/mp4\"," +
                    "\"width\":1920,\"height\":1080,\"frameRate\":\"24\"," +
                    "\"bandwidth\":3423702,\"initRange\":\"0-740\",\"indexRange\":\"741-1048\"}";

            // Act
            VideoStreamMetadataDTO dto = objectMapper.readValue(json, VideoStreamMetadataDTO.class);

            // Assert
            assertEquals("video-1", dto.getId());
            assertEquals("https://example.com/video.mp4", dto.getUrl());
            assertEquals("avc1.640028", dto.getCodec());
            assertEquals("video/mp4", dto.getMimeType());
            assertEquals(1920, dto.getWidth());
            assertEquals(1080, dto.getHeight());
            assertEquals("24", dto.getFrameRate());
            assertEquals(3423702, dto.getBandwidth());
        }

        @Test
        @DisplayName("Should omit null fields in JSON")
        void testSerialization_OmitNullFields() throws Exception {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertFalse(json.contains("initRange"));
            assertFalse(json.contains("indexRange"));
            assertFalse(json.contains("format"));
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTripSerialization() throws Exception {
            // Arrange
            VideoStreamMetadataDTO original = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .initRange("0-740")
                    .indexRange("741-1048")
                    .format("MPEG_4")
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(original);
            VideoStreamMetadataDTO deserialized = objectMapper.readValue(json, VideoStreamMetadataDTO.class);

            // Assert
            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getUrl(), deserialized.getUrl());
            assertEquals(original.getCodec(), deserialized.getCodec());
            assertEquals(original.getWidth(), deserialized.getWidth());
            assertEquals(original.getHeight(), deserialized.getHeight());
            assertEquals(original.getBandwidth(), deserialized.getBandwidth());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should produce readable toString output")
        void testToString() {
            // Arrange
            VideoStreamMetadataDTO dto = VideoStreamMetadataDTO.builder()
                    .id("video-1")
                    .url("https://example.com/video.mp4")
                    .codec("avc1.640028")
                    .mimeType("video/mp4")
                    .width(1920)
                    .height(1080)
                    .frameRate("24")
                    .bandwidth(3423702)
                    .build();

            // Act
            String result = dto.toString();

            // Assert
            assertTrue(result.contains("video-1"));
            assertTrue(result.contains("1920"));
            assertTrue(result.contains("1080"));
            assertTrue(result.contains("24"));
            assertTrue(result.contains("3423702"));
        }
    }
}