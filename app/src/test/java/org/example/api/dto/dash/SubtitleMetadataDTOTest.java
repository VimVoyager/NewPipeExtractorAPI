package org.example.api.dto.dash;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.MediaFormat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for SubtitleMetadataDTO.
 * Tests validation, factory methods, builder pattern, and JSON serialization.
 */
@DisplayName("SubtitleMetadataDTO Tests")
class SubtitleMetadataDTOTest {

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
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .languageName("English")
                    .mimeType("text/vtt")
                    .kind("subtitles")
                    .bandwidth(256)
                    .format("VTT")
                    .build();

            // Assert
            assertEquals("subtitle-1", dto.getId());
            assertEquals("https://example.com/subtitle.vtt", dto.getUrl());
            assertEquals("en", dto.getLanguage());
            assertEquals("English", dto.getLanguageName());
            assertEquals("text/vtt", dto.getMimeType());
            assertEquals("subtitles", dto.getKind());
            assertEquals(256, dto.getBandwidth());
            assertEquals("VTT", dto.getFormat());
        }

        @Test
        @DisplayName("Should build DTO with required fields only")
        void testBuilder_RequiredFieldsOnly() {
            // Act
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .mimeType("text/vtt")
                    .build();

            // Assert
            assertEquals("subtitle-1", dto.getId());
            assertNull(dto.getLanguageName());
            assertNull(dto.getKind());
            assertEquals(256, dto.getBandwidth()); // Default value
            assertNull(dto.getFormat());
        }

        @Test
        @DisplayName("Should use default bandwidth if not specified")
        void testBuilder_DefaultBandwidth() {
            // Act
            SubtitleMetadataDTO dto = new SubtitleMetadataDTO();

            // Assert
            assertEquals(256, dto.getBandwidth());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation for valid DTO")
        void testValidation_ValidDto() {
            // Arrange
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .languageName("English")
                    .mimeType("text/vtt")
                    .kind("subtitles")
                    .bandwidth(256)
                    .build();

            // Act
            Set<ConstraintViolation<SubtitleMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
        }

        @Test
        @DisplayName("Should fail validation when id is blank")
        void testValidation_BlankId() {
            // Arrange
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .mimeType("text/vtt")
                    .build();

            // Act
            Set<ConstraintViolation<SubtitleMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Subtitle ID cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when url is blank")
        void testValidation_BlankUrl() {
            // Arrange
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("")
                    .language("en")
                    .mimeType("text/vtt")
                    .build();

            // Act
            Set<ConstraintViolation<SubtitleMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Subtitle URL cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when language is blank")
        void testValidation_BlankLanguage() {
            // Arrange
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("")
                    .mimeType("text/vtt")
                    .build();

            // Act
            Set<ConstraintViolation<SubtitleMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Subtitle language cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when mimeType is blank")
        void testValidation_BlankMimeType() {
            // Arrange
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .mimeType("")
                    .build();

            // Act
            Set<ConstraintViolation<SubtitleMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Subtitle MIME type cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when bandwidth is zero")
        void testValidation_ZeroBandwidth() {
            // Arrange
            SubtitleMetadataDTO dto = new SubtitleMetadataDTO();
            dto.setBandwidth(0);

            // Act
            Set<ConstraintViolation<SubtitleMetadataDTO>> violations = validator.validate(dto);

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
        @DisplayName("Should create DTO from SubtitlesStream with VTT format")
        void testFrom_VttFormat() {
            // Arrange
            SubtitlesStream stream = mock(SubtitlesStream.class);
            Locale locale = Locale.ENGLISH;

            when(stream.getContent()).thenReturn("https://example.com/subtitle.vtt");
            when(stream.getLocale()).thenReturn(locale);
            when(stream.getDisplayLanguageName()).thenReturn("English");
            when(stream.getFormat()).thenReturn(MediaFormat.VTT);
            when(stream.isAutoGenerated()).thenReturn(false);

            // Act
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("subtitle-1", dto.getId());
            assertEquals("https://example.com/subtitle.vtt", dto.getUrl());
            assertEquals("en", dto.getLanguage());
            assertEquals("English", dto.getLanguageName());
            assertEquals("text/vtt", dto.getMimeType());
            assertEquals("subtitles", dto.getKind());
            assertEquals(256, dto.getBandwidth());
            assertEquals("WebVTT", dto.getFormat());
        }

        @Test
        @DisplayName("Should create DTO from SubtitlesStream with SRT format")
        void testFrom_SrtFormat() {
            // Arrange
            SubtitlesStream stream = mock(SubtitlesStream.class);
            Locale locale = Locale.ENGLISH;

            when(stream.getContent()).thenReturn("https://example.com/subtitle.srt");
            when(stream.getLocale()).thenReturn(locale);
            when(stream.getDisplayLanguageName()).thenReturn("English");
            when(stream.getFormat()).thenReturn(MediaFormat.SRT);
            when(stream.isAutoGenerated()).thenReturn(false);

            // Act
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("application/x-subrip", dto.getMimeType());
            assertEquals("SubRip file format", dto.getFormat());
        }

        @Test
        @DisplayName("Should default to TTML for unknown format")
        void testFrom_UnknownFormat() {
            // Arrange
            SubtitlesStream stream = mock(SubtitlesStream.class);
            Locale locale = Locale.ENGLISH;

            when(stream.getContent()).thenReturn("https://example.com/subtitle.xml");
            when(stream.getLocale()).thenReturn(locale);
            when(stream.getDisplayLanguageName()).thenReturn("English");
            when(stream.getFormat()).thenReturn(null);
            when(stream.isAutoGenerated()).thenReturn(false);

            // Act
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("application/ttml+xml", dto.getMimeType());
        }

        @Test
        @DisplayName("Should set kind to 'asr' for auto-generated subtitles")
        void testFrom_AutoGenerated() {
            // Arrange
            SubtitlesStream stream = mock(SubtitlesStream.class);
            Locale locale = Locale.ENGLISH;

            when(stream.getContent()).thenReturn("https://example.com/subtitle.vtt");
            when(stream.getLocale()).thenReturn(locale);
            when(stream.getDisplayLanguageName()).thenReturn("English");
            when(stream.getFormat()).thenReturn(MediaFormat.VTT);
            when(stream.isAutoGenerated()).thenReturn(true);

            // Act
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("asr", dto.getKind());
        }

        @Test
        @DisplayName("Should handle SubtitlesStream with null locale")
        void testFrom_NullLocale() {
            // Arrange
            SubtitlesStream stream = mock(SubtitlesStream.class);

            when(stream.getContent()).thenReturn("https://example.com/subtitle.vtt");
            when(stream.getLocale()).thenReturn(null);
            when(stream.getDisplayLanguageName()).thenReturn(null);
            when(stream.getFormat()).thenReturn(MediaFormat.VTT);
            when(stream.isAutoGenerated()).thenReturn(false);

            // Act
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("en", dto.getLanguage()); // Default
            assertEquals("English", dto.getLanguageName()); // Default
        }

        @Test
        @DisplayName("Should use display name when provided")
        void testFrom_WithDisplayName() {
            // Arrange
            SubtitlesStream stream = mock(SubtitlesStream.class);
            Locale locale = Locale.FRENCH;

            when(stream.getContent()).thenReturn("https://example.com/subtitle.vtt");
            when(stream.getLocale()).thenReturn(locale);
            when(stream.getDisplayLanguageName()).thenReturn("Français (France)");
            when(stream.getFormat()).thenReturn(MediaFormat.VTT);
            when(stream.isAutoGenerated()).thenReturn(false);

            // Act
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("fr", dto.getLanguage());
            assertEquals("Français (France)", dto.getLanguageName());
        }

        @Test
        @DisplayName("Should throw exception when SubtitlesStream is null")
        void testFrom_NullSubtitlesStream() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    SubtitleMetadataDTO.from(null, 1)
            );

            assertTrue(exception.getMessage().contains("SubtitlesStream cannot be null"));
        }

        @Test
        @DisplayName("Should map various language codes correctly")
        void testFrom_VariousLanguages() {
            // Test Spanish
            SubtitlesStream streamEs = createMockSubtitlesStream(Locale.forLanguageTag("es"));
            SubtitleMetadataDTO dtoEs = SubtitleMetadataDTO.from(streamEs, 1);
            assertEquals("es", dtoEs.getLanguage());
            assertEquals("Spanish", dtoEs.getLanguageName());

            // Test German
            SubtitlesStream streamDe = createMockSubtitlesStream(Locale.GERMAN);
            SubtitleMetadataDTO dtoDe = SubtitleMetadataDTO.from(streamDe, 2);
            assertEquals("de", dtoDe.getLanguage());
            assertEquals("German", dtoDe.getLanguageName());

            // Test Japanese
            SubtitlesStream streamJa = createMockSubtitlesStream(Locale.JAPANESE);
            SubtitleMetadataDTO dtoJa = SubtitleMetadataDTO.from(streamJa, 3);
            assertEquals("ja", dtoJa.getLanguage());
            assertEquals("Japanese", dtoJa.getLanguageName());
        }

        private SubtitlesStream createMockSubtitlesStream(Locale locale) {
            SubtitlesStream stream = mock(SubtitlesStream.class);
            when(stream.getContent()).thenReturn("https://example.com/subtitle.vtt");
            when(stream.getLocale()).thenReturn(locale);
            when(stream.getDisplayLanguageName()).thenReturn(null);
            when(stream.getFormat()).thenReturn(MediaFormat.VTT);
            when(stream.isAutoGenerated()).thenReturn(false);
            return stream;
        }

        @Test
        @DisplayName("Should generate sequential IDs")
        void testFrom_SequentialIds() {
            // Arrange
            SubtitlesStream stream = createMockSubtitlesStream(Locale.ENGLISH);

            // Act
            SubtitleMetadataDTO dto1 = SubtitleMetadataDTO.from(stream, 1);
            SubtitleMetadataDTO dto2 = SubtitleMetadataDTO.from(stream, 2);
            SubtitleMetadataDTO dto3 = SubtitleMetadataDTO.from(stream, 5);

            // Assert
            assertEquals("subtitle-1", dto1.getId());
            assertEquals("subtitle-2", dto2.getId());
            assertEquals("subtitle-5", dto3.getId());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize DTO to JSON")
        void testSerialization() throws Exception {
            // Arrange
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .languageName("English")
                    .mimeType("text/vtt")
                    .kind("subtitles")
                    .bandwidth(256)
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertNotNull(json);
            assertTrue(json.contains("\"id\":\"subtitle-1\""));
            assertTrue(json.contains("\"language\":\"en\""));
            assertTrue(json.contains("\"mimeType\":\"text/vtt\""));
            assertTrue(json.contains("\"kind\":\"subtitles\""));
        }

        @Test
        @DisplayName("Should deserialize JSON to DTO")
        void testDeserialization() throws Exception {
            // Arrange
            String json = "{\"id\":\"subtitle-1\",\"url\":\"https://example.com/subtitle.vtt\"," +
                    "\"language\":\"en\",\"languageName\":\"English\"," +
                    "\"mimeType\":\"text/vtt\",\"kind\":\"subtitles\",\"bandwidth\":256}";

            // Act
            SubtitleMetadataDTO dto = objectMapper.readValue(json, SubtitleMetadataDTO.class);

            // Assert
            assertEquals("subtitle-1", dto.getId());
            assertEquals("https://example.com/subtitle.vtt", dto.getUrl());
            assertEquals("en", dto.getLanguage());
            assertEquals("English", dto.getLanguageName());
            assertEquals("text/vtt", dto.getMimeType());
            assertEquals("subtitles", dto.getKind());
            assertEquals(256, dto.getBandwidth());
        }

        @Test
        @DisplayName("Should omit null fields in JSON")
        void testSerialization_OmitNullFields() throws Exception {
            // Arrange
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .mimeType("text/vtt")
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertFalse(json.contains("languageName"));
            assertFalse(json.contains("kind"));
            assertFalse(json.contains("format"));
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTripSerialization() throws Exception {
            // Arrange
            SubtitleMetadataDTO original = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .languageName("English")
                    .mimeType("text/vtt")
                    .kind("subtitles")
                    .bandwidth(256)
                    .format("VTT")
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(original);
            SubtitleMetadataDTO deserialized = objectMapper.readValue(json, SubtitleMetadataDTO.class);

            // Assert
            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getUrl(), deserialized.getUrl());
            assertEquals(original.getLanguage(), deserialized.getLanguage());
            assertEquals(original.getMimeType(), deserialized.getMimeType());
            assertEquals(original.getKind(), deserialized.getKind());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should produce readable toString output")
        void testToString() {
            // Arrange
            SubtitleMetadataDTO dto = SubtitleMetadataDTO.builder()
                    .id("subtitle-1")
                    .url("https://example.com/subtitle.vtt")
                    .language("en")
                    .languageName("English")
                    .mimeType("text/vtt")
                    .kind("subtitles")
                    .build();

            // Act
            String result = dto.toString();

            // Assert
            assertTrue(result.contains("subtitle-1"));
            assertTrue(result.contains("en"));
            assertTrue(result.contains("English"));
            assertTrue(result.contains("text/vtt"));
            assertTrue(result.contains("subtitles"));
        }
    }
}