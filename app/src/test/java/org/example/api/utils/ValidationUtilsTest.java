package org.example.api.utils;

import org.example.api.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ValidationUtils.
 * Tests all validation methods and edge cases.
 */
@DisplayName("ValidationUtils Tests")
class ValidationUtilsTest {

    @Nested
    @DisplayName("requireNonEmpty Tests")
    class RequireNonEmptyTests {

        @Test
        @DisplayName("Should pass when value is valid")
        void testRequireNonEmpty_ValidValue() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireNonEmpty("valid value", "testField")
            );
        }

        @Test
        @DisplayName("Should throw when value is null")
        void testRequireNonEmpty_NullValue() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty(null, "testField")
            );

            assertTrue(exception.getMessage().contains("testField"));
            assertTrue(exception.getMessage().contains("must not be empty"));
            assertEquals(400, exception.getHttpStatus());
            assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        }

        @Test
        @DisplayName("Should throw when value is empty string")
        void testRequireNonEmpty_EmptyString() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty("", "testField")
            );

            assertTrue(exception.getMessage().contains("testField"));
        }

        @Test
        @DisplayName("Should throw when value is only whitespace")
        void testRequireNonEmpty_WhitespaceOnly() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty("   ", "testField")
            );

            assertTrue(exception.getMessage().contains("testField"));
        }

        @Test
        @DisplayName("Should pass when value has leading/trailing spaces but content")
        void testRequireNonEmpty_WithSpaces() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireNonEmpty("  content  ", "testField")
            );
        }

        @Test
        @DisplayName("Should include field name in error message")
        void testRequireNonEmpty_ErrorMessageContainsFieldName() {
            // Act
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty(null, "myCustomField")
            );

            // Assert
            assertTrue(exception.getMessage().contains("myCustomField"));
        }
    }

    @Nested
    @DisplayName("requireValidUrl Tests")
    class RequireValidUrlTests {

        @Test
        @DisplayName("Should pass for valid HTTP URL")
        void testRequireValidUrl_ValidHttp() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireValidUrl("http://example.com")
            );
        }

        @Test
        @DisplayName("Should pass for valid HTTPS URL")
        void testRequireValidUrl_ValidHttps() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireValidUrl("https://example.com")
            );
        }

        @Test
        @DisplayName("Should pass for valid YouTube URL")
        void testRequireValidUrl_ValidYouTube() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireValidUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            );
        }

        @Test
        @DisplayName("Should throw when URL is null")
        void testRequireValidUrl_Null() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireValidUrl(null)
            );

            assertTrue(exception.getMessage().contains("URL"));
            assertTrue(exception.getMessage().contains("must not be empty"));
        }

        @Test
        @DisplayName("Should throw when URL is empty")
        void testRequireValidUrl_Empty() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireValidUrl("")
            );

            assertTrue(exception.getMessage().contains("URL"));
        }

        @Test
        @DisplayName("Should throw when URL lacks protocol")
        void testRequireValidUrl_NoProtocol() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireValidUrl("example.com")
            );

            assertTrue(exception.getMessage().contains("http"));
        }

        @Test
        @DisplayName("Should throw when URL has invalid protocol")
        void testRequireValidUrl_InvalidProtocol() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireValidUrl("ftp://example.com")
            );

            assertTrue(exception.getMessage().contains("http"));
        }

        @Test
        @DisplayName("Should pass for URL with query parameters")
        void testRequireValidUrl_WithQueryParams() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireValidUrl("https://example.com/path?param1=value1&param2=value2")
            );
        }

        @Test
        @DisplayName("Should pass for URL with fragments")
        void testRequireValidUrl_WithFragment() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireValidUrl("https://example.com/path#section")
            );
        }

        @Test
        @DisplayName("Should pass for URL with port")
        void testRequireValidUrl_WithPort() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireValidUrl("https://example.com:8080/path")
            );
        }
    }

    @Nested
    @DisplayName("requireValidServiceId Tests")
    class RequireValidServiceIdTests {

        @Test
        @DisplayName("Should pass for valid service ID (0)")
        void testRequireValidServiceId_Zero() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireValidServiceId(0)
            );
        }

        @Test
        @DisplayName("Should pass for valid service ID (positive)")
        void testRequireValidServiceId_Positive() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() -> {
                ValidationUtils.requireValidServiceId(1);
                ValidationUtils.requireValidServiceId(10);
                ValidationUtils.requireValidServiceId(100);
            });
        }

        @Test
        @DisplayName("Should throw for negative service ID")
        void testRequireValidServiceId_Negative() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireValidServiceId(-1)
            );

            assertTrue(exception.getMessage().contains("serviceId"));
            assertTrue(exception.getMessage().contains("non-negative"));
        }

        @Test
        @DisplayName("Should throw for large negative service ID")
        void testRequireValidServiceId_LargeNegative() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireValidServiceId(-100)
            );

            assertTrue(exception.getMessage().contains("serviceId"));
        }
    }

    @Nested
    @DisplayName("Exception Details Tests")
    class ExceptionDetailsTests {

        @Test
        @DisplayName("ValidationException should have correct HTTP status")
        void testValidationException_HttpStatus() {
            // Act
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty(null, "field")
            );

            // Assert
            assertEquals(400, exception.getHttpStatus());
        }

        @Test
        @DisplayName("ValidationException should have correct error code")
        void testValidationException_ErrorCode() {
            // Act
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty(null, "field")
            );

            // Assert
            assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        }

        @Test
        @DisplayName("ValidationException message should be descriptive")
        void testValidationException_DescriptiveMessage() {
            // Act
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty(null, "searchString")
            );

            // Assert
            String message = exception.getMessage();
            assertTrue(message.contains("searchString"));
            assertTrue(message.contains("must not be empty"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle special characters in field name")
        void testRequireNonEmpty_SpecialCharsInFieldName() {
            // Act
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty(null, "field_with-special.chars")
            );

            // Assert
            assertTrue(exception.getMessage().contains("field_with-special.chars"));
        }

        @Test
        @DisplayName("Should handle URLs with encoded characters")
        void testRequireValidUrl_EncodedCharacters() {
            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireValidUrl("https://example.com/path?query=hello%20world")
            );
        }

        @Test
        @DisplayName("Should handle very long valid strings")
        void testRequireNonEmpty_VeryLongString() {
            // Arrange
            String longString = "a".repeat(10000);

            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    ValidationUtils.requireNonEmpty(longString, "field")
            );
        }

        @Test
        @DisplayName("Should handle mixed whitespace")
        void testRequireNonEmpty_MixedWhitespace() {
            // Act & Assert
            ValidationException exception = assertThrows(ValidationException.class, () ->
                    ValidationUtils.requireNonEmpty(" \t\n\r ", "field")
            );

            assertTrue(exception.getMessage().contains("field"));
        }
    }
}