package org.example.api.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.example.api.model.Error;
import org.example.api.utils.SerializationUtils;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        .addSerializer(SearchInfo.class, new SerializationUtils.CustomSearchInfoSerializer())
                        .addSerializer(StreamingService.class, new SerializationUtils.CustomStreamingServiceSerializer())
                );
    }

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
