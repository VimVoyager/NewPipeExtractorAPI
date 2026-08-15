package org.example.api.exception;

public class NoStreamsAvailableException extends ApiException {
    public NoStreamsAvailableException(String message) {
        super(message, "NO_STREAMS_AVAILABLE", 503);
    }
}
