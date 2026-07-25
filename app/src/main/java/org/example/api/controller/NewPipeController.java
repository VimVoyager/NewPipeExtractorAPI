package org.example.api.controller;

import org.example.api.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
            errorResponse.put("message", "Error retrieving services: %s".formatted(e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
