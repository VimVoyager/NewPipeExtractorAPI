package org.example.api.dto.dash;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
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
 * Test suite for AudioStreamMetadataDTO.
 * Tests validation, factory methods, builder pattern, and JSON serialization.
 */
@DisplayName("AudioStreamMetadataDTO Tests")
class AudioStreamMetadataDTOTest {

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
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .language("en")
                    .languageName("English")
                    .initRange("0-722")
                    .indexRange("723-898")
                    .format("M4A")
                    .build();

            // Assert
            assertEquals("audio-1", dto.getId());
            assertEquals("https://example.com/audio.mp4", dto.getUrl());
            assertEquals("mp4a.40.2", dto.getCodec());
            assertEquals("audio/mp4", dto.getMimeType());
            assertEquals(130482, dto.getBandwidth());
            assertEquals("44100", dto.getAudioSamplingRate());
            assertEquals(2, dto.getAudioChannels());
            assertEquals("en", dto.getLanguage());
            assertEquals("English", dto.getLanguageName());
            assertEquals("0-722", dto.getInitRange());
            assertEquals("723-898", dto.getIndexRange());
            assertEquals("M4A", dto.getFormat());
        }

        @Test
        @DisplayName("Should build DTO with required fields only")
        void testBuilder_RequiredFieldsOnly() {
            // Act
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .build();

            // Assert
            assertEquals("audio-1", dto.getId());
            assertNull(dto.getLanguage());
            assertNull(dto.getLanguageName());
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
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .language("en")
                    .languageName("English")
                    .build();

            // Act
            Set<ConstraintViolation<AudioStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
        }

        @Test
        @DisplayName("Should fail validation when id is blank")
        void testValidation_BlankId() {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .build();

            // Act
            Set<ConstraintViolation<AudioStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Audio stream ID cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when url is blank")
        void testValidation_BlankUrl() {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .build();

            // Act
            Set<ConstraintViolation<AudioStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Audio stream URL cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when codec is blank")
        void testValidation_BlankCodec() {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .build();

            // Act
            Set<ConstraintViolation<AudioStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Audio codec cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when mimeType is blank")
        void testValidation_BlankMimeType() {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .build();

            // Act
            Set<ConstraintViolation<AudioStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Audio MIME type cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when audioSamplingRate is blank")
        void testValidation_BlankSamplingRate() {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("")
                    .audioChannels(2)
                    .build();

            // Act
            Set<ConstraintViolation<AudioStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Audio sampling rate cannot be blank")));
        }

        @Test
        @DisplayName("Should fail validation when bandwidth is zero")
        void testValidation_ZeroBandwidth() {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(0)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .build();

            // Act
            Set<ConstraintViolation<AudioStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Bandwidth must be at least 1")));
        }

        @Test
        @DisplayName("Should fail validation when audioChannels is zero")
        void testValidation_ZeroChannels() {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(0)
                    .build();

            // Act
            Set<ConstraintViolation<AudioStreamMetadataDTO>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getMessage().contains("Audio channels must be at least 1")));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from AudioStream")
        void testFrom_ValidAudioStream() {
            // Arrange
            AudioStream stream = mock(AudioStream.class);
            MediaFormat format = mock(MediaFormat.class);
            ItagItem itagItem = mock(ItagItem.class);
            Locale locale = Locale.ENGLISH;

            when(stream.getContent()).thenReturn("https://example.com/audio.mp4");
            when(stream.getCodec()).thenReturn("mp4a.40.2");
            when(stream.getFormat()).thenReturn(format);
            when(format.getMimeType()).thenReturn("audio/mp4");
            when(format.getName()).thenReturn("M4A");
            when(stream.getBitrate()).thenReturn(130482);
            when(stream.getItagItem()).thenReturn(itagItem);
            when(itagItem.getSampleRate()).thenReturn(44100);
            when(itagItem.getAudioChannels()).thenReturn(2);
            when(stream.getAudioLocale()).thenReturn(locale);
            when(stream.getInitStart()).thenReturn(0);
            when(stream.getInitEnd()).thenReturn(722);
            when(stream.getIndexStart()).thenReturn(723);
            when(stream.getIndexEnd()).thenReturn(898);

            // Act
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("audio-1", dto.getId());
            assertEquals("https://example.com/audio.mp4", dto.getUrl());
            assertEquals("mp4a.40.2", dto.getCodec());
            assertEquals("audio/mp4", dto.getMimeType());
            assertEquals(130482, dto.getBandwidth());
            assertEquals("44100", dto.getAudioSamplingRate());
            assertEquals(2, dto.getAudioChannels());
            assertEquals("en", dto.getLanguage());
            assertEquals("English", dto.getLanguageName());
            assertEquals("0-722", dto.getInitRange());
            assertEquals("723-898", dto.getIndexRange());
            assertEquals("M4A", dto.getFormat());
        }

        @Test
        @DisplayName("Should handle AudioStream with null locale")
        void testFrom_NullLocale() {
            // Arrange
            AudioStream stream = mock(AudioStream.class);
            MediaFormat format = mock(MediaFormat.class);
            ItagItem itagItem = mock(ItagItem.class);

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

            // Act
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.from(stream, 1);

            // Assert
            assertEquals("und", dto.getLanguage());
            assertEquals("Unknown", dto.getLanguageName());
        }

        @Test
        @DisplayName("Should handle AudioStream with null format")
        void testFrom_NullFormat() {
            // Arrange
            AudioStream stream = mock(AudioStream.class);
            ItagItem itagItem = mock(ItagItem.class);

            when(stream.getContent()).thenReturn("https://example.com/audio.mp4");
            when(stream.getCodec()).thenReturn("mp4a.40.2");
            when(stream.getFormat()).thenReturn(null);
            when(stream.getBitrate()).thenReturn(130482);
            when(stream.getItagItem()).thenReturn(itagItem);
            when(itagItem.getSampleRate()).thenReturn(44100);
            when(itagItem.getAudioChannels()).thenReturn(2);
            when(stream.getAudioLocale()).thenReturn(null);
            when(stream.getInitStart()).thenReturn(-1);
            when(stream.getIndexStart()).thenReturn(-1);

            // Act
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.from(stream, 2);

            // Assert
            assertEquals("audio/mp4", dto.getMimeType()); // Default
            assertNull(dto.getFormat());
        }

        @Test
        @DisplayName("Should throw exception when AudioStream is null")
        void testFrom_NullAudioStream() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    AudioStreamMetadataDTO.from(null, 1)
            );

            assertTrue(exception.getMessage().contains("AudioStream cannot be null"));
        }

        @Test
        @DisplayName("Should default to 2 channels when not specified")
        void testFrom_DefaultChannels() {
            // Arrange
            AudioStream stream = mock(AudioStream.class);
            MediaFormat format = mock(MediaFormat.class);
            ItagItem itagItem = mock(ItagItem.class);

            when(stream.getContent()).thenReturn("https://example.com/audio.mp4");
            when(stream.getCodec()).thenReturn("mp4a.40.2");
            when(stream.getFormat()).thenReturn(format);
            when(format.getMimeType()).thenReturn("audio/mp4");
            when(stream.getBitrate()).thenReturn(130482);
            when(stream.getItagItem()).thenReturn(itagItem);
            when(itagItem.getSampleRate()).thenReturn(44100);
            when(itagItem.getAudioChannels()).thenReturn(0); // Not specified
            when(stream.getAudioLocale()).thenReturn(null);
            when(stream.getInitStart()).thenReturn(-1);
            when(stream.getIndexStart()).thenReturn(-1);

            // Act
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.from(stream, 1);

            // Assert
            assertEquals(2, dto.getAudioChannels()); // Default to stereo
        }

        @Test
        @DisplayName("Should map various language codes correctly")
        void testFrom_VariousLanguages() {
            // Test Spanish
            AudioStream streamEs = createMockAudioStream(Locale.forLanguageTag("es"));
            AudioStreamMetadataDTO dtoEs = AudioStreamMetadataDTO.from(streamEs, 1);
            assertEquals("es", dtoEs.getLanguage());
            assertEquals("Spanish", dtoEs.getLanguageName());

            // Test French
            AudioStream streamFr = createMockAudioStream(Locale.FRENCH);
            AudioStreamMetadataDTO dtoFr = AudioStreamMetadataDTO.from(streamFr, 2);
            assertEquals("fr", dtoFr.getLanguage());
            assertEquals("French", dtoFr.getLanguageName());

            // Test German
            AudioStream streamDe = createMockAudioStream(Locale.GERMAN);
            AudioStreamMetadataDTO dtoDe = AudioStreamMetadataDTO.from(streamDe, 3);
            assertEquals("de", dtoDe.getLanguage());
            assertEquals("German", dtoDe.getLanguageName());
        }

        private AudioStream createMockAudioStream(Locale locale) {
            AudioStream stream = mock(AudioStream.class);
            MediaFormat format = mock(MediaFormat.class);
            ItagItem itagItem = mock(ItagItem.class);

            when(stream.getContent()).thenReturn("https://example.com/audio.mp4");
            when(stream.getCodec()).thenReturn("mp4a.40.2");
            when(stream.getFormat()).thenReturn(format);
            when(format.getMimeType()).thenReturn("audio/mp4");
            when(stream.getBitrate()).thenReturn(130482);
            when(stream.getItagItem()).thenReturn(itagItem);
            when(itagItem.getSampleRate()).thenReturn(44100);
            when(itagItem.getAudioChannels()).thenReturn(2);
            when(stream.getAudioLocale()).thenReturn(locale);
            when(stream.getInitStart()).thenReturn(-1);
            when(stream.getIndexStart()).thenReturn(-1);

            return stream;
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize DTO to JSON")
        void testSerialization() throws Exception {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .language("en")
                    .languageName("English")
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertNotNull(json);
            assertTrue(json.contains("\"id\":\"audio-1\""));
            assertTrue(json.contains("\"bandwidth\":130482"));
            assertTrue(json.contains("\"audioChannels\":2"));
            assertTrue(json.contains("\"language\":\"en\""));
        }

        @Test
        @DisplayName("Should deserialize JSON to DTO")
        void testDeserialization() throws Exception {
            // Arrange
            String json = "{\"id\":\"audio-1\",\"url\":\"https://example.com/audio.mp4\"," +
                    "\"codec\":\"mp4a.40.2\",\"mimeType\":\"audio/mp4\"," +
                    "\"bandwidth\":130482,\"audioSamplingRate\":\"44100\"," +
                    "\"audioChannels\":2,\"language\":\"en\",\"languageName\":\"English\"}";

            // Act
            AudioStreamMetadataDTO dto = objectMapper.readValue(json, AudioStreamMetadataDTO.class);

            // Assert
            assertEquals("audio-1", dto.getId());
            assertEquals("https://example.com/audio.mp4", dto.getUrl());
            assertEquals("mp4a.40.2", dto.getCodec());
            assertEquals("audio/mp4", dto.getMimeType());
            assertEquals(130482, dto.getBandwidth());
            assertEquals("44100", dto.getAudioSamplingRate());
            assertEquals(2, dto.getAudioChannels());
            assertEquals("en", dto.getLanguage());
            assertEquals("English", dto.getLanguageName());
        }

        @Test
        @DisplayName("Should omit null fields in JSON")
        void testSerialization_OmitNullFields() throws Exception {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertFalse(json.contains("language"));
            assertFalse(json.contains("languageName"));
            assertFalse(json.contains("initRange"));
            assertFalse(json.contains("format"));
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTripSerialization() throws Exception {
            // Arrange
            AudioStreamMetadataDTO original = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .language("en")
                    .languageName("English")
                    .initRange("0-722")
                    .indexRange("723-898")
                    .format("M4A")
                    .build();

            // Act
            String json = objectMapper.writeValueAsString(original);
            AudioStreamMetadataDTO deserialized = objectMapper.readValue(json, AudioStreamMetadataDTO.class);

            // Assert
            assertEquals(original.getId(), deserialized.getId());
            assertEquals(original.getUrl(), deserialized.getUrl());
            assertEquals(original.getCodec(), deserialized.getCodec());
            assertEquals(original.getBandwidth(), deserialized.getBandwidth());
            assertEquals(original.getLanguage(), deserialized.getLanguage());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should produce readable toString output")
        void testToString() {
            // Arrange
            AudioStreamMetadataDTO dto = AudioStreamMetadataDTO.builder()
                    .id("audio-1")
                    .url("https://example.com/audio.mp4")
                    .codec("mp4a.40.2")
                    .mimeType("audio/mp4")
                    .bandwidth(130482)
                    .audioSamplingRate("44100")
                    .audioChannels(2)
                    .language("en")
                    .languageName("English")
                    .build();

            // Act
            String result = dto.toString();

            // Assert
            assertTrue(result.contains("audio-1"));
            assertTrue(result.contains("mp4a.40.2"));
            assertTrue(result.contains("130482"));
            assertTrue(result.contains("44100"));
            assertTrue(result.contains("2"));
            assertTrue(result.contains("en"));
            assertTrue(result.contains("English"));
        }
    }
}