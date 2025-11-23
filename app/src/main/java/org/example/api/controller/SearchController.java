package org.example.api.controller;

import org.example.api.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * REST controller for handling search-related API requests for YouTube information.
 * This class provides endpoints for searching videos based on various parameters
 * such as service ID and search string. All endpoints are prefixed with "/api/v1/search".
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    /**
     * Constructs a new SearchController with the specified SearchService.
     *
     * @param searchService an instance of SearchService used to handle search operations.
     */
    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Handles HTTP GET requests to retrieve search information based on the provided service ID and search string.
     *
     * @param searchString   the string to use for searching; must not be null or empty.
     * @param sortFilter     an optional parameter to sort the results; can be null.
     * @param contentFilters  an optional comma-separated list of content filters to apply; can be null.
     * @return a ResponseEntity containing either:
     *         - A success response with the search information in JSON format (HTTP 200 OK),
     *         - A bad request response if the search string is missing (HTTP 400 Bad Request),
     *         - An error response if an internal error occurs (HTTP 500 Internal Server Error).
     * @throws Exception if an unexpected error occurs while retrieving search information.
     */
    @GetMapping("/")
    public ResponseEntity<?> getSearchInfo(
            @RequestParam(name = "searchString") String searchString,
            @RequestParam(name = "sortFilter", required = false) String sortFilter,
            @RequestParam(name = "contentFilters", required = false) String contentFilters
    ) throws Exception {
        try {
            logger.info("Retrieving search info for searchString: {}", searchString);

            if (searchString == null || searchString.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("message", "Search string is required"));
            }

            List<String> contentFilterList = contentFilters != null && !contentFilters.isEmpty()
                    ? Arrays.asList(contentFilters.split(","))
                    : Collections.emptyList();

            String searchInfoJson = searchService.getSearchInfo(
                    searchString,
                    contentFilterList,
                    sortFilter
            );

            if (searchInfoJson == null || searchInfoJson.contains("\"message\"")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error retrieving search info");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            return ResponseEntity.ok(searchInfoJson);
        } catch (Exception e) {
            logger.error("Error retrieving search info for searchString: {}",
                    searchString, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving search info",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Handles HTTP GET requests to retrieve a search page based on the provided parameters.
     *
     * @param searchString   the string to use for searching; must not be null or empty.
     * @param sortFilter     an optional parameter to sort the results; can be null.
     * @param contentFilters  an optional comma-separated list of content filters to apply; can be null.
     * @param pageUrl       the URL of the page to retrieve; must not be null or empty.
     * @return a ResponseEntity containing either:
     *         - A success response with the search page information in JSON format (HTTP 200 OK),
     *         - A bad request response if the search string or page URL is missing (HTTP 400 Bad Request),
     *         - An error response if an internal error occurs (HTTP 500 Internal Server
     *         - An error response if an internal error occurs (HTTP 500 Internal Server Error).
     * @throws Exception if an unexpected error occurs while retrieving the search page.
     */
    @GetMapping("/page")
    public ResponseEntity<?> getSearchPage(
            @RequestParam(name = "searchString") String searchString,
            @RequestParam(name = "sortFilter", required = false) String sortFilter,
            @RequestParam(name = "contentFilters", required = false) String contentFilters,
            @RequestParam(name = "pageUrl") String pageUrl
    ) throws Exception {
        try {
            logger.info("Retrieving search page for searchString: {}, pageUrl: {}", searchString, pageUrl);

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
            logger.error("Error retrieving search page for searchString: {}, pageUrl: {}",
                    searchString, pageUrl, e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error retrieving search page",
                            "details", e.getMessage()
                    ));
        }
    }
}
