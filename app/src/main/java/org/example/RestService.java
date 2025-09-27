package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.GsonBuilder;
import org.example.DownloaderImpl;

import org.jspecify.annotations.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

public class RestService {

    private final ObjectMapper objectMapper;

    public RestService() {
        DownloaderImpl downloader = DownloaderImpl.init(null);
        NewPipe.init(downloader, new Localization("en", "GB"));

        this.objectMapper = new ObjectMapper()
                // Ignore properties that can't be serialized
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // Allow unknown properties
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Register a mix-in to help with serialization
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

            // Basic info
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
            // gen.writeStringField("serviceName", value.getServiceName());
            gen.writeEndObject();
        }
    }

    // Additional method to handle complex serialization with more control
    private String safeSerialize(Object obj) {
        try {
            // Use a specialized ObjectWriter for more controlled serialization
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

    public String getServices() {
        try {
            return objectMapper.writeValueAsString(NewPipe.getServices());
        } catch (Exception e) {
            System.err.println("Search info Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    // Modify your existing methods to use this approach
    public String getSearchInfo(int serviceId, String searchString, List<String> contentFilters, String sortFilter) throws ParsingException, ExtractionException, IOException {
        try {
            StreamingService service = NewPipe.getService(serviceId);
            SearchInfo info = SearchInfo.getInfo(service, service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter));

            // Additional logging for debugging
            System.out.println("Search Info retrieved successfully");
            System.out.println("Total items: " + (info.getRelatedItems() != null ? info.getRelatedItems().size() : "0"));

            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            // Comprehensive error logging
            System.err.println("Search Info Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }


    // Similar approach for other methods that return complex objects
    public String getStreamInfo(@NonNull String url) throws IOException, ExtractionException {
        try {
            StreamInfo info = StreamInfo.getInfo(url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            System.err.println("Stream Info Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    // Example usage in other methods
    public String getSearchPage(int serviceId, String searchString, List<String> contentFilters, String sortFilter, String pageUrl) {
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

    public String getKioskIdsList(int serviceId) throws ExtractionException {
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

    public String getKioskInfo(int serviceId, String kioskId) throws ExtractionException, IOException {
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

    public String getKioskPage(int serviceId, String kioskId, String pageUrl) throws ExtractionException, IOException {
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

    public String getPlaylistInfo(@NonNull String url) throws IOException, ExtractionException {
        try {
            PlaylistInfo info = PlaylistInfo.getInfo(url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getPlaylistPage(@NonNull String url, @NonNull String pageUrl) throws IOException, ExtractionException {
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

    public String getChannelInfo(@NonNull String url) throws IOException, ExtractionException {
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

    public String getCommentsInfo(@NonNull String url) throws IOException, ExtractionException {
        try {
            CommentsInfo info = CommentsInfo.getInfo(url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getCommentsPage(@NonNull String url, @NonNull String pageUrl) throws IOException, ExtractionException {
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
