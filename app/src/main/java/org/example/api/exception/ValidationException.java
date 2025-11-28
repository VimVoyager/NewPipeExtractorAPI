package org.example.api.exception;

/**
 * Exception thrown when request validation fails.
 */
public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR", 400);
    }

    public ValidationException(String field, String reason) {
        super(String.format("Invalid %s: %s", field, reason), "VALIDATION_ERROR", 400);
    }
}

