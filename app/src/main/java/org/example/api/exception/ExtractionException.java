package org.example.api.exception;

/**
 * Exception thrown when resource extraction fails.
 */
public class ExtractionException extends ApiException {
    public ExtractionException(String message) {
        super(message, "EXTRACTION_ERROR", 500);
    }

    public ExtractionException(String message, Throwable cause) {
        super(message, "EXTRACTION_ERROR", 500, cause);
    }
}