package org.example.api.controller;

import org.example.api.service.VideoStreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling streaming-related API requests for YouTube information.
 * This class provides endpoints for retrieving stream and audio stream data based on provided IDs.
 * All endpoints are prefixed with "/api/v1/streams".
 */
@RestController
@RequestMapping("/api/v1/streams")
public class StreamingController {
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final Logger logger = LoggerFactory.getLogger(StreamingController.class);
    private final VideoStreamingService videoStreamingService;

    @Autowired
    public StreamingController(VideoStreamingService videoStreamingService) {
        this.videoStreamingService = videoStreamingService;
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
     * @return A ResponseEntity object containing either:
     *                           - A success response with the stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/")
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
     * @return A ResponseEntity object containing either:
     *                           - A success response with the audio stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/audio")
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
     * @return A ResponseEntity object containing either:
     *                           - A success response with the video stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/video")
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
     * @return A ResponseEntity object containing either:
     *                           - A success response with the subtitle stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/subtitles")
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
     * @return  A ResponseEntity object containing either:
     *                           - A success response with the stream segments in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/segments")
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
     * @return A ResponseEntity object containing either:
     *                           - A success response with the preview frames in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/frames")
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
     * @return  A ResponseEntity object containing either:
     *                           - A success response with the stream description in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping("/description")
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
}
