package org.example.api.controller;

import org.example.api.service.RestService;
import org.example.api.service.SearchService;
import org.example.api.service.VideoStreamingService;
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
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final Logger logger = LoggerFactory.getLogger(NewPipeController.class);
    private final RestService restService;
    private final VideoStreamingService videoStreamingService;
    private final SearchService searchService;

    @Autowired
    public NewPipeController(RestService restService, VideoStreamingService videoStreamingService, SearchService searchService) {
        this.restService = restService;
        this.videoStreamingService = videoStreamingService;
        this.searchService = searchService;

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
     * @return ResponseEntity<?> A ResponseEntity object containing either:
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

    /**
     * Handles HTTP GET requests to retrieve stream information based on a provided ID.
     *
     * This endpoint requires an ID as a request parameter. It constructs a URL using
     * the ID and interacts with the RestService to fetch stream information.
     * On success, a ResponseEntity with the stream information in JSON format
     * is returned (HTTP 200 OK). If the ID parameter is missing or if an error occurs
     * during retrieval, an appropriate error message is returned (HTTP 400 Bad Request
     * or HTTP 500 Internal Server Error).
     *
     * @param id The ID of the stream to retrieve information for.
     * @return ResponseEntity<?> A ResponseEntity object containing either:
     *                           - A success response with the stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/streams")
    public ResponseEntity<?> getStreamInfo(@RequestParam(name = "id") String id) {
        try {
            logger.info("Retrieving stream info for ID: {}", id);

            // Validate URL
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "ID parameter is required"));
            }

            String url = YOUTUBE_URL + id;
            String streamInfoJson = videoStreamingService.getStreamInfo(url);

            // Check if it's an error response
            if (streamInfoJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving stream info");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(streamInfoJson);
        } catch (Exception e) {
            logger.error("Error retrieving stream info for ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving stream info",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Handles HTTP GET requests to retrieve audio stream information based on a provided ID.
     *
     * This endpoint requires an ID as a request parameter. It constructs a URL using
     * the ID and interacts with the RestService to fetch audio stream information.
     * On success, a ResponseEntity with the audio streams in JSON format
     * is returned (HTTP 200 OK). If the ID parameter is missing or if an error occurs
     * during retrieval, an appropriate error message is returned (HTTP 400 Bad Request
     * or HTTP 500 Internal Server Error).
     *
     * @param id The ID of the stream to retrieve audio information for.
     * @return ResponseEntity<?> A ResponseEntity object containing either:
     *                           - A success response with the audio stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/streams/audio")
    public ResponseEntity<?> getAudioStreams(@RequestParam(name = "id") String id) throws Exception {
        try {
            logger.info("Retrieving audio stream for ID: {}", id);

            // Validate URL
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "ID parameter is required"));
            }

            String url = YOUTUBE_URL + id;
            String audioStreamsJson = videoStreamingService.getAudioStreams(url);

            // Check if it's an error response
            if (audioStreamsJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving audio streams");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(audioStreamsJson);
        } catch (Exception e) {

            logger.error("Error retrieving audio stream for ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving audio stream",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Handles HTTP GET requests to retrieve video stream information based on a provided ID.
     *
     * This endpoint requires an ID as a request parameter. It constructs a URL using
     * the ID and interacts with the RestService to fetch video stream information.
     * On success, a ResponseEntity with the video streams in JSON format
     * is returned (HTTP 200 OK). If the ID parameter is missing or if an error occurs
     * during retrieval, an appropriate error message is returned (HTTP 400 Bad Request
     * or HTTP 500 Internal Server Error).
     *
     * @param id The ID of the stream to retrieve video information for.
     * @return ResponseEntity<?> A ResponseEntity object containing either:
     *                           - A success response with the video stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/streams/video")
    public ResponseEntity<?> getVideoStreams(@RequestParam(name = "id") String id) throws Exception {
        try {
            logger.info("Retrieving video stream for ID: {}", id);

            // Validate URL
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "ID parameter is required"));
            }

            String url = YOUTUBE_URL + id;
            String videoStreamsJson = videoStreamingService.getVideoStreams(url);

            // Check if it's an error response
            if (videoStreamsJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving video streams");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(videoStreamsJson);
        } catch (Exception e) {

            logger.error("Error retrieving video stream for ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving video stream info",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Handles HTTP GET requests to retrieve subtitle stream information based on a provided ID.
     *
     * This endpoint requires an ID as a request parameter. It constructs a URL using
     * the ID and interacts with the RestService to fetch subtitle stream information.
     * On success, a ResponseEntity with the subtitle streams in JSON format
     * is returned (HTTP 200 OK). If the ID parameter is missing or if an error occurs
     * during retrieval, an appropriate error message is returned (HTTP 400 Bad Request
     * or HTTP 500 Internal Server Error).
     *
     * @param id The ID of the stream to retrieve subtitle information for.
     * @return ResponseEntity<?> A ResponseEntity object containing either:
     *                           - A success response with the subtitle stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/streams/subtitles")
    public ResponseEntity<?> getSubtitleStreams(@RequestParam(name = "id") String id) throws Exception {
        try {
            logger.info("Retrieving subtitle stream for ID: {}", id);

            // Validate URL
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "ID parameter is required"));
            }

            String url = YOUTUBE_URL + id;
            String subtitleStreamsJson = videoStreamingService.getSubtitleStreams(url);

            // Check if it's an error response
            if (subtitleStreamsJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving subtitle streams");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(subtitleStreamsJson);
        } catch (Exception e) {

            logger.error("Error retrieving subtitles for ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving stream info",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Handles HTTP GET requests to retrieve stream segments based on a provided ID.
     *
     * This endpoint requires an ID as a request parameter. It constructs a URL using
     * the ID and interacts with the RestService to fetch stream segments.
     * On success, a ResponseEntity with the stream segments in JSON format
     * is returned (HTTP 200 OK). If the ID parameter is missing or if an error occurs
     * during retrieval, an appropriate error message is returned (HTTP 400 Bad Request
     * or HTTP 500 Internal Server Error).
     *
     * @param id The ID of the stream to retrieve segments for.
     * @return ResponseEntity<?> A ResponseEntity object containing either:
     *                           - A success response with the stream segments in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/streams/segments")
    public ResponseEntity<?> getStreamSegments(@RequestParam(name = "id") String id) throws Exception {
        try {
            logger.info("Retrieving stream segments for ID: {}", id);

            // Validate URL
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "ID parameter is required"));
            }

            String url = YOUTUBE_URL + id;
            String streamSegmentsJson = videoStreamingService.getStreamSegments(url);

            // Check if it's an error response
            if (streamSegmentsJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving stream segments");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(streamSegmentsJson);
        } catch (Exception e) {

            logger.error("Error retrieving stream segments for ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving stream segments",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Handles HTTP GET requests to retrieve preview frames for a stream based on a provided ID.
     *
     * This endpoint requires an ID as a request parameter. It constructs a URL using
     * the ID and interacts with the RestService to fetch preview frames.
     * On success, a ResponseEntity with the preview frames in JSON format
     * is returned (HTTP 200 OK). If the ID parameter is missing or if an error occurs
     * during retrieval, an appropriate error message is returned (HTTP 400 Bad Request
     * or HTTP 500 Internal Server Error).
     *
     * @param id The ID of the stream to retrieve preview frames for.
     * @return ResponseEntity<?> A ResponseEntity object containing either:
     *                           - A success response with the preview frames in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/streams/frames")
    public ResponseEntity<?> getPreviewFrames(@RequestParam(name = "id") String id) throws Exception {
        try {
            logger.info("Retrieving preview frames stream for ID: {}", id);

            // Validate URL
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "ID parameter is required"));
            }

            String url = YOUTUBE_URL + id;
            String streamFramesJson = videoStreamingService.getPreviewFrames(url);

            // Check if it's an error response
            if (streamFramesJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving preview frames");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(streamFramesJson);
        } catch (Exception e) {

            logger.error("Error retrieving preview frames for ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving preview frames",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Handles HTTP GET requests to retrieve the description of a stream based on a provided ID.
     *
     * This endpoint requires an ID as a request parameter. It constructs a URL using
     * the ID and interacts with the RestService to fetch the stream description.
     * On success, a ResponseEntity with the stream description in JSON format
     * is returned (HTTP 200 OK). If the ID parameter is missing or if an error occurs
     * during retrieval, an appropriate error message is returned (HTTP 400 Bad Request
     * or HTTP 500 Internal Server Error).
     *
     * @param id The ID of the stream to retrieve description for.
     * @return ResponseEntity<?> A ResponseEntity object containing either:
     *                           - A success response with the stream description in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/streams/description")
    public ResponseEntity<?> getStreamDescription(@RequestParam(name = "id") String id) throws Exception {
        try {
            logger.info("Retrieving description stream for ID: {}", id);

            // Validate URL
            if (id == null || id.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "ID parameter is required"));
            }

            String url = YOUTUBE_URL + id;
            String streamDescriptionJson = videoStreamingService.getStreamDescription(url);

            // Check if it's an error response
            if (streamDescriptionJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving description streams");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(streamDescriptionJson);
        } catch (Exception e) {

            logger.error("Error retrieving description for ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving description",
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

            String searchInfoJson = searchService.getSearchInfo(
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

            String searchPageJson = searchService.getSearchPage(
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
