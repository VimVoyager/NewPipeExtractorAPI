package org.example.api.controller;

import org.example.api.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for handling API requests for YouTube information from New Pipe Extractor.
 *
 * This class defines endpoints for fetching various types of stream data, such as
 * audio streams, video streams, subtitles, and more, from YouTube using a specified ID.
 * Utilizes the RestService to abstract the logic of interacting with YouTube.
 * All endpoints are prefixed with "/api/v1".
 *
 * <p>
 * The following constants and fields are defined:
 * </p>
 * <ul>
 *    <li><b>YOUTUBE_URL</b>: A constant URL string that serves as the base for constructing
 *        full URLs to YouTube videos based on their IDs.</li>
 *    <li><b>logger</b>: A logger instance for logging events and errors within the controller.</li>
 *    <li><b>restService</b>: An instance of RestService used to interact with YouTube for
 *        retrieving stream data.</li>
 * </ul>
 *
 * <p>
 * The constructor uses Spring's dependency injection to initialize the RestService instance.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
public class NewPipeController {
    private static final Logger logger = LoggerFactory.getLogger(NewPipeController.class);
    private final RestService restService;

    @Autowired
    public NewPipeController(RestService restService) {
        this.restService = restService;
    }

    /**
     * Handles HTTP GET requests to retrieve a list of services.
     *
     * This endpoint interacts with a RestService to fetch the services' data.
     * If the data retrieval is successful, a ResponseEntity containing the
     * services in JSON format is returned with an HTTP 200 OK status.
     *
     * In case of any errors during the retrieval process, an appropriate error
     * message is logged, and a ResponseEntity with an HTTP 500 Internal Server Error
     * status is returned, containing a message about the failure.
     *
     * @return A ResponseEntity object containing either:
     *                           - A success response with the services data in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - An error response with an appropriate message (HTTP 500 Internal Server Error) if an error occurs during the process.
     */
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
