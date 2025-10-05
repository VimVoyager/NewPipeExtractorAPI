package org.example.api.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Utility class for custom JSON serialization of complex objects.
 *
 * This class provides specialized serialization methods to handle
 * complex objects from the NewPipe extractor, ensuring safe and
 * controlled JSON conversion.
 */
@Component
public class SerializationUtils {
    private final ObjectMapper objectMapper;

    public SerializationUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper; // Inject ObjectMapper if needed
    }

    /**
     * Custom JSON serializer for SearchInfo objects.
     *
     * This serializer provides a controlled way to convert SearchInfo
     * objects to JSON, limiting the depth and including only essential
     * information.
     */
    public static class CustomSearchInfoSerializer extends JsonSerializer<SearchInfo> {

        /**
         * Serialize a SearchInfo object with minimal, essential information.
         *
         * @param value The SearchInfo object to serialize
         * @param gen JsonGenerator for writing JSON content
         * @param serializers Serializer provider
         * @throws IOException If an error occurs during serialization
         */
        @Override
        public void serialize(SearchInfo value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("searchString", value.getSearchString());
            gen.writeNumberField("serviceId", value.getService() != null ? value.getService().getServiceId() : -1);

            // Carefully serialize items
            gen.writeArrayFieldStart("items");
            if (value.getRelatedItems() != null) {
                for (InfoItem item : value.getRelatedItems()) {
                    // Minimal serialization of items
                    gen.writeStartObject();
                    if (item != null) {
                        gen.writeStringField("name", item.getName());
                        gen.writeStringField("url", item.getUrl());
                        // Add other essential fields as needed
                    }
                    gen.writeEndObject();
                }
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }

    /**
     * Custom JSON serializer for StreamingService objects.
     *
     * This serializer provides a simplified JSON representation
     * of StreamingService, avoiding nested serialization.
     */
    public static class CustomStreamingServiceSerializer extends JsonSerializer<StreamingService> {

        /**
         * Serialize a StreamingService object with minimal information.
         *
         * @param value The StreamingService object to serialize
         * @param gen JsonGenerator for writing JSON content
         * @param serializers Serializer provider
         * @throws IOException If an error occurs during serialization
         */
        @Override
        public void serialize(StreamingService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("serviceId", value.getServiceId());
            gen.writeEndObject();
        }
    }

    /**
     * Safely serialize an object to a JSON string.
     *
     * This method provides a robust way to convert objects to JSON,
     * with error handling and pretty-printing.
     *
     * @param obj The object to be serialized
     * @return A JSON string representation of the object
     */
    public String safeSerialize(Object obj) {
        try {
            ObjectWriter writer = objectMapper.writer()
                    .without(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .withDefaultPrettyPrinter();

            return writer.writeValueAsString(obj);
        } catch (Exception e) {
            System.err.println("Serialization Error: " + e.getMessage());
            e.printStackTrace();
            return "{\"message\":\"Error serializing response\"}";
        }
    }
}
