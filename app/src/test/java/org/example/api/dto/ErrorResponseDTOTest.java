package org.example.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ErrorResponseDTO.
 * Tests constructors, JSON serialization, and error response structure.
 */
@DisplayName("ErrorResponseDTO Tests")
class ErrorResponseDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create DTO with default constructor")
        void testDefaultConstructor() {
            // Act
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Assert
            assertNotNull(dto.getTimestamp());
            assertNotNull(Instant.parse(dto.getTimestamp())); // Verify valid ISO timestamp
        }

        @Test
        @DisplayName("Should create DTO with all parameters")
        void testParameterizedConstructor() {
            // Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    404,
                    "Not Found",
                    "Video not found",
                    "VIDEO_NOT_FOUND"
            );

            // Assert
            assertNotNull(dto.getTimestamp());
            assertEquals(404, dto.getStatus());
            assertEquals("Not Found", dto.getError());
            assertEquals("Video not found", dto.getMessage());
            assertEquals("VIDEO_NOT_FOUND", dto.getErrorCode());
        }

        @Test
        @DisplayName("Should generate timestamp on construction")
        void testTimestampGeneration() throws InterruptedException {
            // Arrange
            ErrorResponseDTO dto1 = new ErrorResponseDTO();
            Thread.sleep(10); // Small delay to ensure different timestamps
            ErrorResponseDTO dto2 = new ErrorResponseDTO();

            // Assert
            assertNotEquals(dto1.getTimestamp(), dto2.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set timestamp")
        void testTimestamp() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();
            String timestamp = "2025-12-03T10:15:30Z";

            // Act
            dto.setTimestamp(timestamp);

            // Assert
            assertEquals(timestamp, dto.getTimestamp());
        }

        @Test
        @DisplayName("Should get and set status")
        void testStatus() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setStatus(500);

            // Assert
            assertEquals(500, dto.getStatus());
        }

        @Test
        @DisplayName("Should get and set error")
        void testError() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setError("Internal Server Error");

            // Assert
            assertEquals("Internal Server Error", dto.getError());
        }

        @Test
        @DisplayName("Should get and set message")
        void testMessage() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setMessage("An unexpected error occurred");

            // Assert
            assertEquals("An unexpected error occurred", dto.getMessage());
        }

        @Test
        @DisplayName("Should get and set errorCode")
        void testErrorCode() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setErrorCode("INTERNAL_ERROR");

            // Assert
            assertEquals("INTERNAL_ERROR", dto.getErrorCode());
        }

        @Test
        @DisplayName("Should get and set path")
        void testPath() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setPath("/api/v1/streams");

            // Assert
            assertEquals("/api/v1/streams", dto.getPath());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize to JSON with all fields")
        void testSerialize_AllFields() throws Exception {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    400,
                    "Bad Request",
                    "Invalid video ID",
                    "INVALID_VIDEO_ID"
            );
            dto.setPath("/api/v1/streams");

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"status\":400"));
            assertTrue(json.contains("\"error\":\"Bad Request\""));
            assertTrue(json.contains("\"message\":\"Invalid video ID\""));
            assertTrue(json.contains("\"errorCode\":\"INVALID_VIDEO_ID\""));
            assertTrue(json.contains("\"path\":\"/api/v1/streams\""));
            assertTrue(json.contains("\"timestamp\""));
        }

        @Test
        @DisplayName("Should deserialize from JSON")
        void testDeserialize() throws Exception {
            // Arrange
            String json = """
                {
                    "timestamp": "2025-12-03T10:15:30Z",
                    "status": 404,
                    "error": "Not Found",
                    "message": "Video not found",
                    "errorCode": "VIDEO_NOT_FOUND",
                    "path": "/api/v1/streams/details"
                }
                """;

            // Act
            ErrorResponseDTO dto = objectMapper.readValue(json, ErrorResponseDTO.class);

            // Assert
            assertEquals("2025-12-03T10:15:30Z", dto.getTimestamp());
            assertEquals(404, dto.getStatus());
            assertEquals("Not Found", dto.getError());
            assertEquals("Video not found", dto.getMessage());
            assertEquals("VIDEO_NOT_FOUND", dto.getErrorCode());
            assertEquals("/api/v1/streams/details", dto.getPath());
        }

        @Test
        @DisplayName("Should exclude null fields from JSON")
        void testSerialize_NullFieldsExcluded() throws Exception {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();
            dto.setStatus(500);
            dto.setError("Internal Server Error");
            // message, errorCode, and path are null

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"status\":500"));
            assertTrue(json.contains("\"error\":\"Internal Server Error\""));
            assertFalse(json.contains("\"message\""));
            assertFalse(json.contains("\"errorCode\""));
            assertFalse(json.contains("\"path\""));
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTrip() throws Exception {
            // Arrange
            ErrorResponseDTO original = new ErrorResponseDTO(
                    403,
                    "Forbidden",
                    "Access denied",
                    "ACCESS_DENIED"
            );
            original.setPath("/api/v1/restricted");

            // Act
            String json = objectMapper.writeValueAsString(original);
            ErrorResponseDTO deserialized = objectMapper.readValue(json, ErrorResponseDTO.class);

            // Assert
            assertEquals(original.getStatus(), deserialized.getStatus());
            assertEquals(original.getError(), deserialized.getError());
            assertEquals(original.getMessage(), deserialized.getMessage());
            assertEquals(original.getErrorCode(), deserialized.getErrorCode());
            assertEquals(original.getPath(), deserialized.getPath());
        }
    }

    @Nested
    @DisplayName("HTTP Status Code Tests")
    class HttpStatusTests {

        @Test
        @DisplayName("Should handle 400 Bad Request")
        void test400BadRequest() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    400,
                    "Bad Request",
                    "Missing required parameter",
                    "MISSING_PARAMETER"
            );

            // Assert
            assertEquals(400, dto.getStatus());
            assertEquals("Bad Request", dto.getError());
        }

        @Test
        @DisplayName("Should handle 404 Not Found")
        void test404NotFound() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    404,
                    "Not Found",
                    "Resource not found",
                    "NOT_FOUND"
            );

            // Assert
            assertEquals(404, dto.getStatus());
            assertEquals("Not Found", dto.getError());
        }

        @Test
        @DisplayName("Should handle 429 Too Many Requests")
        void test429TooManyRequests() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    429,
                    "Too Many Requests",
                    "Rate limit exceeded",
                    "RATE_LIMIT_EXCEEDED"
            );

            // Assert
            assertEquals(429, dto.getStatus());
            assertEquals("Too Many Requests", dto.getError());
        }

        @Test
        @DisplayName("Should handle 500 Internal Server Error")
        void test500InternalServerError() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    500,
                    "Internal Server Error",
                    "An unexpected error occurred",
                    "INTERNAL_ERROR"
            );

            // Assert
            assertEquals(500, dto.getStatus());
            assertEquals("Internal Server Error", dto.getError());
        }

        @Test
        @DisplayName("Should handle 503 Service Unavailable")
        void test503ServiceUnavailable() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    503,
                    "Service Unavailable",
                    "Service is temporarily unavailable",
                    "SERVICE_UNAVAILABLE"
            );

            // Assert
            assertEquals(503, dto.getStatus());
            assertEquals("Service Unavailable", dto.getError());
        }
    }

    @Nested
    @DisplayName("Error Code Tests")
    class ErrorCodeTests {

        @Test
        @DisplayName("Should handle validation error codes")
        void testValidationErrorCodes() {
            // Arrange & Act
            ErrorResponseDTO dto1 = new ErrorResponseDTO(400, "Bad Request", "Invalid ID", "INVALID_ID");
            ErrorResponseDTO dto2 = new ErrorResponseDTO(400, "Bad Request", "Missing field", "MISSING_FIELD");

            // Assert
            assertEquals("INVALID_ID", dto1.getErrorCode());
            assertEquals("MISSING_FIELD", dto2.getErrorCode());
        }

        @Test
        @DisplayName("Should handle resource error codes")
        void testResourceErrorCodes() {
            // Arrange & Act
            ErrorResponseDTO dto1 = new ErrorResponseDTO(404, "Not Found", "Video not found", "VIDEO_NOT_FOUND");
            ErrorResponseDTO dto2 = new ErrorResponseDTO(404, "Not Found", "Channel not found", "CHANNEL_NOT_FOUND");

            // Assert
            assertEquals("VIDEO_NOT_FOUND", dto1.getErrorCode());
            assertEquals("CHANNEL_NOT_FOUND", dto2.getErrorCode());
        }

        @Test
        @DisplayName("Should handle service error codes")
        void testServiceErrorCodes() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    503,
                    "Service Unavailable",
                    "NewPipe extractor error",
                    "EXTRACTOR_ERROR"
            );

            // Assert
            assertEquals("EXTRACTOR_ERROR", dto.getErrorCode());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setError(null);
            dto.setMessage(null);
            dto.setErrorCode(null);
            dto.setPath(null);

            // Assert
            assertNull(dto.getError());
            assertNull(dto.getMessage());
            assertNull(dto.getErrorCode());
            assertNull(dto.getPath());
        }

        @Test
        @DisplayName("Should handle empty strings")
        void testEmptyStrings() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setError("");
            dto.setMessage("");
            dto.setErrorCode("");
            dto.setPath("");

            // Assert
            assertEquals("", dto.getError());
            assertEquals("", dto.getMessage());
            assertEquals("", dto.getErrorCode());
            assertEquals("", dto.getPath());
        }

        @Test
        @DisplayName("Should handle very long error messages")
        void testLongErrorMessage() {
            // Arrange
            String longMessage = "A".repeat(1000);
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setMessage(longMessage);

            // Assert
            assertEquals(1000, dto.getMessage().length());
            assertEquals(longMessage, dto.getMessage());
        }

        @Test
        @DisplayName("Should handle special characters in messages")
        void testSpecialCharacters() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();
            String specialMessage = "Error: \"Invalid\" <parameter> & value=123";

            // Act
            dto.setMessage(specialMessage);

            // Assert
            assertEquals(specialMessage, dto.getMessage());
        }

        @Test
        @DisplayName("Should handle zero status code")
        void testZeroStatusCode() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setStatus(0);

            // Assert
            assertEquals(0, dto.getStatus());
        }

        @Test
        @DisplayName("Should handle negative status code")
        void testNegativeStatusCode() {
            // Arrange
            ErrorResponseDTO dto = new ErrorResponseDTO();

            // Act
            dto.setStatus(-1);

            // Assert
            assertEquals(-1, dto.getStatus());
        }
    }

    @Nested
    @DisplayName("Practical Usage Tests")
    class PracticalUsageTests {

        @Test
        @DisplayName("Should create complete validation error response")
        void testValidationErrorResponse() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    400,
                    "Bad Request",
                    "Video ID must not be empty",
                    "VALIDATION_ERROR"
            );
            dto.setPath("/api/v1/streams");

            // Assert
            assertEquals(400, dto.getStatus());
            assertEquals("Bad Request", dto.getError());
            assertEquals("Video ID must not be empty", dto.getMessage());
            assertEquals("VALIDATION_ERROR", dto.getErrorCode());
            assertEquals("/api/v1/streams", dto.getPath());
            assertNotNull(dto.getTimestamp());
        }

        @Test
        @DisplayName("Should create complete not found error response")
        void testNotFoundErrorResponse() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    404,
                    "Not Found",
                    "Video with ID 'dQw4w9WgXcQ' not found",
                    "VIDEO_NOT_FOUND"
            );
            dto.setPath("/api/v1/streams/details?id=dQw4w9WgXcQ");

            // Assert
            assertEquals(404, dto.getStatus());
            assertTrue(dto.getMessage().contains("dQw4w9WgXcQ"));
            assertTrue(dto.getPath().contains("id=dQw4w9WgXcQ"));
        }

        @Test
        @DisplayName("Should create complete rate limit error response")
        void testRateLimitErrorResponse() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    429,
                    "Too Many Requests",
                    "Rate limit exceeded. Please try again in 60 seconds.",
                    "RATE_LIMIT_EXCEEDED"
            );
            dto.setPath("/api/v1/search");

            // Assert
            assertEquals(429, dto.getStatus());
            assertTrue(dto.getMessage().contains("60 seconds"));
            assertEquals("RATE_LIMIT_EXCEEDED", dto.getErrorCode());
        }

        @Test
        @DisplayName("Should create complete server error response")
        void testServerErrorResponse() {
            // Arrange & Act
            ErrorResponseDTO dto = new ErrorResponseDTO(
                    500,
                    "Internal Server Error",
                    "Failed to extract video information from YouTube",
                    "EXTRACTION_ERROR"
            );
            dto.setPath("/api/v1/streams");

            // Assert
            assertEquals(500, dto.getStatus());
            assertTrue(dto.getMessage().contains("extract"));
            assertEquals("EXTRACTION_ERROR", dto.getErrorCode());
        }
    }
}