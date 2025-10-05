package org.example.api.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class for handling and serializing error information.
 *
 * Provides methods to consistently format and convert error details
 * to JSON, with a robust and flexible approach to error representation.
 */
public class ErrorUtils {
    private static final ObjectMapper objectMapper = createCustomObjectMapper();

    /**
     * Creates a custom ObjectMapper with specific configuration to
     * handle serialization and deserialization edge cases.
     *
     * @return A configured ObjectMapper with relaxed serialization rules
     */
    private static ObjectMapper createCustomObjectMapper() {
        return new ObjectMapper()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Converts an exception to a JSON-formatted error message.
     *
     * This method extracts the exception message and serializes it
     * into a standard error response format. If the original
     * serialization fails, it returns a fallback error message.
     *
     * @param e The exception to be converted to an error response
     * @return A JSON string representing the error details
     */
    public static String getError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "An error occurred";
        Error errorResponse = new Error(message);
        try {
            return objectMapper.writeValueAsString(errorResponse);
        } catch (Exception ex) {
            return "{\"message\":\"Error serializing error response\"}";
        }
    }
}
