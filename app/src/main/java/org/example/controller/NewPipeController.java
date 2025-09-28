package org.example.controller;

import org.example.service.RestService;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class NewPipeController {
    private static final Logger logger = LoggerFactory.getLogger(NewPipeController.class);
    private final RestService restService;

    @Autowired
    public NewPipeController(RestService restService) {
        this.restService = restService;
    }

    @GetMapping("/services")
    public ResponseEntity<?> getServices() {
        try {
            logger.info("Attempting to retrieve services");

            // Use the RestService method instead of duplicating logic
            String servicesJson = restService.getServices();

            // Check if it's an error response
            if (servicesJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving services");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            // Return the JSON string as-is since RestService already handles serialization
            return ResponseEntity.ok(servicesJson);

        } catch (Exception e) {
            logger.error("Error retrieving services", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error retrieving services: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/streams")
    public ResponseEntity<?> getStreamInfo(@RequestParam(name = "url") String url) {
        try {
            logger.info("Retrieving stream info for URL: {}", url);

            // Validate URL
            if (url == null || url.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "URL parameter is required"));
            }

            String streamInfoJson = restService.getStreamInfo(url);

            // Check if it's an error response
            if (streamInfoJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving stream info");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(streamInfoJson);
        } catch (Exception e) {
            logger.error("Error retrieving stream info for URL: {}", url, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving stream info",
                            "details", e.getMessage()
                    ));
        }
    }


    @GetMapping("/search")
    public ResponseEntity<?> getSearchInfo(
            @RequestParam(name = "serviceId") int serviceId,
            @RequestParam(name = "searchString") String searchString,
            @RequestParam(name = "sortFilter", required = false) String sortFilter,
            @RequestParam(name = "contentFilters", required = false) String contentFilters
    ) throws Exception {
        try {
            logger.info("Retrieving search info for serviceId: {}, searchString: {}", serviceId, searchString);

            if (searchString == null || searchString.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Search string is required"));
            }

            List<String> contentFilterList = contentFilters != null && !contentFilters.isEmpty()
                    ? Arrays.asList(contentFilters.split(","))
                    : Collections.emptyList();

            String searchInfoJson = restService.getSearchInfo(
                    serviceId,
                    searchString,
                    contentFilterList,
                    sortFilter
            );

            if (searchInfoJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving search info");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            return ResponseEntity.ok(searchInfoJson);
        } catch (Exception e) {
            logger.error("Error retrieving search info for serviceId: {}, searchString: {}",
                    serviceId, searchString, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving search info",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/search/page")
    public ResponseEntity<?> getSearchPage(
            @RequestParam(name = "serviceId") int serviceId,
            @RequestParam(name = "searchString") String searchString,
            @RequestParam(name = "sortFilter", required = false) String sortFilter,
            @RequestParam(name = "contentFilters", required = false) String contentFilters,
            @RequestParam(name = "pageUrl") String pageUrl
    ) throws Exception {
        try {
            logger.info("Retrieving search page for serviceId: {}, searchString: {}, pageUrl: {}", serviceId, searchString, pageUrl);

            if (searchString == null || searchString.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Search string is required"));
            }

            if (pageUrl == null || pageUrl.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Page URL is required"));
            }

            List<String> contentFilterList = contentFilters != null && !contentFilters.isEmpty()
                    ? Arrays.asList(contentFilters.split(","))
                    : Collections.emptyList();

            String searchPageJson = restService.getSearchPage(
                    serviceId,
                    searchString,
                    contentFilterList,
                    sortFilter,
                    pageUrl
            );

            if (searchPageJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving search page");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            return ResponseEntity.ok(searchPageJson);
        } catch (Exception e) {
            logger.error("Error retrieving search page for serviceId: {}, searchString: {}, pageUrl: {}",
                    serviceId, searchString, pageUrl, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving search page",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/playlists")
    public ResponseEntity<?> getPlaylistInfo(@RequestParam(name = "url") String url) throws Exception {
        try {
            logger.info("Retrieving playlist info for url: {}", url);

            if (url == null || url.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "URL is required"));
            }

            String playlistInfoJson = restService.getPlaylistInfo(url);

            if (playlistInfoJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving playlist info");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(playlistInfoJson);
        } catch (Exception e) {
            logger.error("Error retrieving playlist info for url: {}", url, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving playlist info",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/playlists/page")
    public ResponseEntity<?> getPlaylistPage(
            @RequestParam(name = "url") String url,
            @RequestParam(name = "pageUrl") String pageUrl
    ) throws Exception {
        try {
            logger.info("Retrieving playlist page for url: {}, pageUrl: {}", url, pageUrl);

            if (url == null || url.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "URL is required"));
            }

            if (pageUrl == null || pageUrl.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Page URL is required"));
            }

            String playlistPageJson = restService.getPlaylistPage(
                    url,
                    pageUrl
            );

            if (playlistPageJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving playlist page");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(playlistPageJson);

        } catch (Exception e) {
            logger.error("Error retrieving playlist page for url: {}, pageUrl: {}", url, pageUrl, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving playlist page",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/channels")
    public ResponseEntity<?> getChannelInfo(@RequestParam(name = "url") String url) throws Exception {
        try {
            logger.info("Retrieving channel info for url: {}", url);

            if (url == null || url.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "URL is required"));
            }

            String channelInfoJson = restService.getChannelInfo(url);

            if (channelInfoJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving chanel info");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(channelInfoJson);
        } catch (Exception e) {
            logger.error("Error retrieving channel info for url: {}", url, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving channel info",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/kiosks")
    public ResponseEntity<?> getKioskIdsList(@RequestParam(name = "serviceId") int serviceId) throws Exception {
        try {
            logger.info("Retrieving kiosk IDs for serviceId: {}", serviceId);

            String kioskIdListJson = restService.getKioskIdsList(serviceId);

            if (kioskIdListJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving kiosk ID list");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(kioskIdListJson);
        } catch (Exception e) {
            logger.error("Error retrieving kiosk ID list for serviceId: {}", serviceId, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving kiosk ID list",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/kiosks/{kioskId}")
    public ResponseEntity<?> getKioskInfo(
            @PathVariable(name = "kioskId") String kioskId,
            @RequestParam(name = "serviceId") int serviceId
    ) throws Exception {
        try {
            logger.info("Retrieving kiosk IDs for kioskId: {}, serviceId: {}", kioskId, serviceId);

            String kioskInfoJson = restService.getKioskInfo(serviceId, kioskId);

            if (kioskInfoJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving kiosk ID list");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(kioskInfoJson);
        } catch (Exception e) {
            logger.error("Error retrieving kiosk ID list for serviceId: {}", serviceId, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving kiosk ID list",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/kiosks/{kioskId}/page")
    public ResponseEntity<?> getKioskInfo(
            @PathVariable(name = "kioskId") String kioskId,
            @RequestParam(name = "serviceId") int serviceId,
            @RequestParam(name = "pageUrl") String pageUrl
    ) throws Exception {
        try {
            logger.info("Retrieving playlist page for kioskId: {}, serviceId: {}, pageUrl: {}", kioskId, serviceId, pageUrl);

            if (kioskId == null || kioskId.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Kiosk Id is required"));
            }

            if (pageUrl == null || pageUrl.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Page URL is required"));
            }

            String kioskPageJson = restService.getKioskPage(
                    serviceId,
                    kioskId,
                    pageUrl
            );

            if (kioskPageJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving playlist page");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(kioskPageJson);

        } catch (Exception e) {
            logger.error("Error retrieving playlist page for kioskId {}, serviceId: {}, pageUrl: {}", kioskId, serviceId, pageUrl, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving kiosk page",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getComments(@RequestParam(name = "url") String url) throws Exception {
        try {
            logger.info("Retrieving comments info for url: {}", url);

            if (url == null || url.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "URL is required"));
            }

            String commentsInfoJson = restService.getCommentsInfo(url);

            if (commentsInfoJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving comments info");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(commentsInfoJson);
        } catch (Exception e) {
            logger.error("Error retrieving comments info for url: {}", url, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving comments info",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/comments/page")
    public ResponseEntity getCommentsPage(
            @RequestParam(name = "url") String url,
            @RequestParam(name = "pageUrl") String pageUrl
    ) throws Exception {
        try {
            logger.info("Retrieving comments page for url: {}, pageUrl: {}", url, pageUrl);

            if (url == null || url.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "URL is required"));
            }

            if (pageUrl == null || pageUrl.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Page URL is required"));
            }

            String commentsPageJson = restService.getCommentsPage(
                    url,
                    pageUrl
            );

            if (commentsPageJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving comments page");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(commentsPageJson);

        } catch (Exception e) {
            logger.error("Error retrieving comments page for url: {}, pageUrl: {}", url, pageUrl, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving comments page",
                            "details", e.getMessage()
                    ));
        }

    }
}
