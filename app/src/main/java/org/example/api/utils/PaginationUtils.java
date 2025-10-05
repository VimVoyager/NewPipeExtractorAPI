package org.example.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.example.api.utils.ErrorUtils.getError;

/**
 * Utility class for handling paginated API responses.
 *
 * Provides a consistent mechanism for extracting and serializing
 * paginated data with robust error handling. This class helps
 * standardize the process of retrieving and converting paginated
 * content across different API endpoints.
 */
@Component
public class PaginationUtils {
    private final ObjectMapper objectMapper;
    private final SerializationUtils serializationUtils;

    /**
     * Constructor to inject dependencies for pagination handling.
     *
     * @param objectMapper The ObjectMapper for JSON processing
     * @param serializationUtils Utility for safe object serialization
     */
    public PaginationUtils(ObjectMapper objectMapper, SerializationUtils serializationUtils) {
        this.objectMapper = objectMapper;
        this.serializationUtils = serializationUtils;
    }

    /**
     * Handles the extraction and serialization of paginated data.
     *
     * This method provides a generic approach to retrieving and
     * converting paginated information, with built-in error handling.
     * It uses a supplier to extract the data, then safely serializes
     * the result or converts any exceptions to a standardized error response.
     *
     * @param infoExtractor A supplier that provides the paginated data
     * @return A JSON string representing the paginated data or an error message
     */
    public String handlePaginatedResponse(Supplier<Object> infoExtractor) {
        try {
            Object info = infoExtractor.get();
            return serializationUtils.safeSerialize(info);
        } catch (Exception e) {
            System.err.println("Pagination Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }
}
