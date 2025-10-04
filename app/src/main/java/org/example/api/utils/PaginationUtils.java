package org.example.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.example.api.utils.ErrorUtils.getError;

@Component
public class PaginationUtils {
    private final ObjectMapper objectMapper;
    private final SerializationUtils serializationUtils;

    public PaginationUtils(ObjectMapper objectMapper, SerializationUtils serializationUtils) {
        this.objectMapper = objectMapper;
        this.serializationUtils = serializationUtils;
    }

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
