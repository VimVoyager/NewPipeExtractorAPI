package org.example.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ErrorResponseDTO.
 *
 * <p>Reduced from 31 tests to 4. This class has two pieces of real
 * behaviour: the no-args constructor stamps a fresh {@link Instant}
 * timestamp, and {@code @JsonInclude(NON_NULL)} omits unset fields from
 * the JSON body. Everything else — every HTTP-status-code test, every
 * error-code test, the getter/setter nest, most of the edge cases, and
 * all four "practical usage" tests — called the same constructor with a
 * different literal int or String, exercising no code this class
 * doesn't share with every other test here. Kept: constructor wiring,
 * the null-omission contract, and one round-trip.</p>
 */
@DisplayName("ErrorResponseDTO Tests")
class ErrorResponseDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("The no-args constructor stamps a fresh, parseable, recent timestamp")
    void defaultConstructorStampsRecentTimestamp() {
        ErrorResponseDTO dto = new ErrorResponseDTO();

        Instant timestamp = Instant.parse(dto.getTimestamp());
        assertThat(timestamp).isCloseTo(Instant.now(), org.assertj.core.api.Assertions.within(5, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("The all-args constructor wires status/error/message/errorCode and still stamps a timestamp")
    void allArgsConstructorWiresFieldsAndStampsTimestamp() {
        ErrorResponseDTO dto = new ErrorResponseDTO(404, "Not Found", "Video not found", "VIDEO_NOT_FOUND");

        assertThat(dto.getTimestamp()).isNotNull();
        assertThat(dto.getStatus()).isEqualTo(404);
        assertThat(dto.getError()).isEqualTo("Not Found");
        assertThat(dto.getMessage()).isEqualTo("Video not found");
        assertThat(dto.getErrorCode()).isEqualTo("VIDEO_NOT_FOUND");
        assertThat(dto.getPath()).isNull(); // only set via setPath(), not the constructor
    }

    @Test
    @DisplayName("Omits unset fields (message, errorCode, path) from the serialized JSON")
    void omitsUnsetFieldsFromJson() throws Exception {
        ErrorResponseDTO dto = new ErrorResponseDTO();
        dto.setStatus(500);
        dto.setError("Internal Server Error");
        // message, errorCode, and path deliberately left unset

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"status\":500", "\"error\":\"Internal Server Error\"");
        assertThat(json).doesNotContain("\"message\"", "\"errorCode\"", "\"path\"");
    }

    @Test
    @DisplayName("Round-trips every field, including path, through serialize/deserialize unchanged")
    void roundTripPreservesAllFields() throws Exception {
        ErrorResponseDTO original = new ErrorResponseDTO(403, "Forbidden", "Access denied", "ACCESS_DENIED");
        original.setPath("/api/v1/restricted");

        ErrorResponseDTO restored = objectMapper.readValue(
                objectMapper.writeValueAsString(original), ErrorResponseDTO.class);

        assertThat(restored.getTimestamp()).isEqualTo(original.getTimestamp());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.getError()).isEqualTo(original.getError());
        assertThat(restored.getMessage()).isEqualTo(original.getMessage());
        assertThat(restored.getErrorCode()).isEqualTo(original.getErrorCode());
        assertThat(restored.getPath()).isEqualTo(original.getPath());
    }
}