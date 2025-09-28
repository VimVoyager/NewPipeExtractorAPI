package org.example.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.example.model.Error;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class RestService {
    private final ObjectMapper objectMapper;

    public RestService() {
        this.objectMapper = createCustomObjectMapper();
    }

    private ObjectMapper createCustomObjectMapper() {
        return new ObjectMapper()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new SimpleModule()
                        .addSerializer(SearchInfo.class, new CustomSearchInfoSerializer())
                        .addSerializer(StreamingService.class, new CustomStreamingServiceSerializer())
                );
    }

    // Custom serializer for SearchInfo to limit depth and control serialization
    private static class CustomSearchInfoSerializer extends JsonSerializer<SearchInfo> {
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
    private static class CustomStreamingServiceSerializer extends JsonSerializer<StreamingService> {
        @Override
        public void serialize(StreamingService value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("serviceId", value.getServiceId());
            gen.writeEndObject();
        }
    }

    // Utility method for safe serialization
    private String safeSerialize(Object obj) {
        try {
            ObjectWriter writer = objectMapper.writer()
                    .without(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .withDefaultPrettyPrinter();

            return writer.writeValueAsString(obj);
        } catch (Exception e) {
            System.err.println("Serialization Error: " + e.getMessage());
            e.printStackTrace();
            return getError(e);
        }
    }

    // Advanced method to handle pagination and complex responses
    public String handlePaginatedResponse(Supplier<Object> infoExtractor) {
        try {
            Object info = infoExtractor.get();
            return safeSerialize(info);
        } catch (Exception e) {
            System.err.println("Pagination Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

//    public String getServices() {
//        try {
//            System.out.println(NewPipe.getServices());
//            return objectMapper.writeValueAsString(NewPipe.getServices());
//        } catch (Exception e) {
//            System.err.println("Search info Extraction Error:");
//            e.printStackTrace();
//            return getError(e);
//        }
//    }

    public String getServices() {
        try {
            // Verify NewPipe initialization
            if (NewPipe.getServices() == null || NewPipe.getServices().isEmpty()) {
                throw new IllegalStateException("NewPipe services are not initialized or empty");
            }

            List<StreamingService> services = NewPipe.getServices();
            System.out.println("Found " + services.size() + " services");

            // Create a simplified list of services
            List<Map<String, Object>> serviceList = new ArrayList<>();
            for (StreamingService service : services) {
                Map<String, Object> serviceMap = new HashMap<>();
                serviceMap.put("serviceId", service.getServiceId());
                serviceMap.put("serviceName", service.getServiceInfo().getName());
                serviceList.add(serviceMap);
            }

            return objectMapper.writeValueAsString(serviceList);
        } catch (Exception e) {
            System.err.println("Services Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    // Similar approach for other methods that return complex objects
    public String getStreamInfo(String url) throws IOException, ExtractionException {
        try {
            StreamInfo info = StreamInfo.getInfo(url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            System.err.println("Stream Info Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    // Modify your existing methods to use this approach
    public String getSearchInfo(int serviceId, String searchString, List<String> contentFilters, String sortFilter) throws Exception {
        try {
            StreamingService service = NewPipe.getService(serviceId);
            SearchInfo info = SearchInfo.getInfo(service, service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter));

            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            // Comprehensive error logging
            System.err.println("Search Info Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    // Example usage in other methods
    public String getSearchPage(int serviceId, String searchString, List<String> contentFilters, String sortFilter, String pageUrl) throws Exception {
        return handlePaginatedResponse(() -> {
            try {
                StreamingService service = NewPipe.getService(serviceId);
                Page pageInstance = new Page(pageUrl);
                return SearchInfo.getMoreItems(
                        service,
                        service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter),
                        pageInstance
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract search page", e);
            }
        });
    }




    public String getPlaylistInfo(String url) throws Exception {
        try {
            PlaylistInfo info = PlaylistInfo.getInfo(url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getPlaylistPage( String url, String pageUrl) throws Exception {
        try {
            StreamingService service = NewPipe.getServiceByUrl(url);
            Page pageInstance = new Page(pageUrl);
            InfoItemsPage<StreamInfoItem> page = PlaylistInfo.getMoreItems(service, url, pageInstance);
            return objectMapper.writeValueAsString(page);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getChannelInfo(String url) throws Exception {
        try {
            ChannelInfo info = ChannelInfo.getInfo(url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

//    public String getChannelPage(@NonNull String url, @NonNull String pageUrl) throws IOException, ExtractionException {
//        StreamingService service = NewPipe.getServiceByUrl(url);
//        InfoItemsPage<StreamInfoItem> page = ChannelInfo.getMoreItems(service, url, pageUrl);
//        return objectManager.writeValueAsString(page);
//    }

    public String getKioskInfo(int serviceId, String kioskId) throws Exception {
        try {
            StreamingService service = NewPipe.getService(serviceId);
            String url = service.getKioskList().getListLinkHandlerFactoryByType(kioskId).getUrl(kioskId);
            KioskInfo info = KioskInfo.getInfo(service, url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getKioskPage(int serviceId, String kioskId, String pageUrl) throws Exception {
        try {
            StreamingService service = NewPipe.getService(serviceId);
            Page pageInstance = new Page(pageUrl);
            String url = service.getKioskList().getListLinkHandlerFactoryByType(kioskId).getUrl(kioskId);
            InfoItemsPage<StreamInfoItem> page = KioskInfo.getMoreItems(service, url, pageInstance);
            return objectMapper.writeValueAsString(page);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getKioskIdsList(int serviceId) throws Exception {
        try {
            StreamingService service = NewPipe.getService(serviceId);
            List<String> res = new ArrayList<>();
            service.getKioskList().getAvailableKiosks().forEach(k -> res.add(k));
            return objectMapper.writeValueAsString(res);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }


    public String getCommentsInfo(String url) throws Exception {
        try {
            CommentsInfo info = CommentsInfo.getInfo(url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getCommentsPage(String url, String pageUrl) throws Exception {
        try {
            //TODO optimize this. init page is fetched every time
            CommentsInfo info = CommentsInfo.getInfo(url);
            Page pageInstance = new Page(pageUrl);
            InfoItemsPage<CommentsInfoItem> page = CommentsInfo.getMoreItems(info, pageInstance);
            return objectMapper.writeValueAsString(page);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "An error occurred";
        Error errorResponse = new Error(message);
        try {
            return objectMapper.writeValueAsString(errorResponse);
        } catch (Exception ex) {
            return "{\"message\":\"Error serializing error response\"}";
        }
    }
}
