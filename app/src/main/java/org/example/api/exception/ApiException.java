package org.example.api.exception;

/**
 * Base exception class for all API-related exceptions.
 * Provides a consistent way to handle errors throughout the application.
 */
public class ApiException extends RuntimeException {
    private final String errorCode;
    private final int httpStatus;

    public ApiException(String message) {
        this(message, "INTERNAL_ERROR", 500);
    }

    public ApiException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public ApiException(String message, Throwable cause) {
        this(message, "INTERNAL_ERROR", 500, cause);
    }

    public ApiException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}