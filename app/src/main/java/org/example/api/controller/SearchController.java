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

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/")
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

    @GetMapping("/page")
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
}
