package org.example.api.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SerializationUtils {
    private final ObjectMapper objectMapper;

    public SerializationUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper; // Inject ObjectMapper if needed
    }

    // Custom serializer for SearchInfo to limit depth and control serialization
    public static class CustomSearchInfoSerializer extends JsonSerializer<SearchInfo> {
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

    // Custom serializer for StreamingService to avoid nested serialization
    public static class CustomStreamingServiceSerializer extends JsonSerializer<StreamingService> {
        @Override
        public void serialize(StreamingService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("serviceId", value.getServiceId());
            gen.writeEndObject();
        }
    }

    // Utility method for safe serialization
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
