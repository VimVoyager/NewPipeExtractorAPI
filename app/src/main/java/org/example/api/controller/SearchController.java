package org.example.api.controller;

import org.example.api.dto.SearchPageDTO;
import org.example.api.dto.SearchResultDTO;
import org.example.api.service.SearchService;
import org.example.api.utils.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * REST controller for handling search-related API requests.
 * All endpoints are prefixed with "/api/v1/search".
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Performs a search and returns the initial results.
     *
     * @param searchString The search query
     * @param sortFilter Optional sort filter
     * @param contentFilters Optional comma-separated content filters
     * @return SearchResultDTO with search results
     */
    @GetMapping
    public ResponseEntity<SearchResultDTO> search(
            @RequestParam(name = "searchString") String searchString,
            @RequestParam(name = "sortFilter", required = false) String sortFilter,
            @RequestParam(name = "contentFilters", required = false) String contentFilters
    ) {
        logger.info("Search request received for: {}", searchString);

        ValidationUtils.requireNonEmpty(searchString, "searchString");

        List<String> contentFilterList = parseContentFilters(contentFilters);

        SearchResultDTO results = searchService.getSearchInfo(
                searchString,
                contentFilterList,
                sortFilter
        );

        return ResponseEntity.ok(results);
    }

    /**
     * Retrieves a specific page of search results.
     *
     * @param searchString The search query
     * @param pageUrl The page URL for pagination
     * @param sortFilter Optional sort filter
     * @param contentFilters Optional comma-separated content filters
     * @return SearchPageDTO with the requested page of results
     */
    @GetMapping("/page")
    public ResponseEntity<SearchPageDTO> searchPage(
            @RequestParam(name = "searchString") String searchString,
            @RequestParam(name = "pageUrl") String pageUrl,
            @RequestParam(name = "sortFilter", required = false) String sortFilter,
            @RequestParam(name = "contentFilters", required = false) String contentFilters
    ) {
        logger.info("Search page request for: {} with pageUrl: {}", searchString, pageUrl);

        ValidationUtils.requireNonEmpty(searchString, "searchString");
        ValidationUtils.requireNonEmpty(pageUrl, "pageUrl");

        List<String> contentFilterList = parseContentFilters(contentFilters);

        SearchPageDTO page = searchService.getSearchPage(
                searchString,
                contentFilterList,
                sortFilter,
                pageUrl
        );

        return ResponseEntity.ok(page);
    }

    /**
     * Parses comma-separated content filters into a list.
     *
     * @param contentFilters Comma-separated filter string
     * @return List of filter strings, or empty list if null/empty
     */
    private List<String> parseContentFilters(String contentFilters) {
        if (contentFilters == null || contentFilters.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(contentFilters.split(","));
    }
}
