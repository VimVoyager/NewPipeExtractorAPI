package org.example.api.service;

import org.example.api.dto.SearchPageDTO;
import org.example.api.dto.SearchResultDTO;
import org.example.api.dto.SearchItemDTO;
import org.example.api.exception.ExtractionException;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for performing and managing search operations across
 * different streaming services using NewPipe extractor.
 *
 * This service provides methods to retrieve initial search results
 * and paginated search results.
 */
@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static final int YOUTUBE_SERVICE_ID = 0;

    /**
     * Performs an initial search and returns deduplicated results.
     *
     * @param searchString The query string for the search
     * @param contentFilters List of filters to refine search results
     * @param sortFilter Sorting method for the search results
     * @return SearchResultDTO containing search results
     * @throws ExtractionException if search extraction fails
     */
    public SearchResultDTO getSearchInfo(
            String searchString,
            List<String> contentFilters,
            String sortFilter
    ) {
        try {
            logger.info("Performing search for: {}", searchString);

            StreamingService service = NewPipe.getService(YOUTUBE_SERVICE_ID);
            SearchInfo info = SearchInfo.getInfo(
                    service,
                    service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter)
            );

            SearchResultDTO dto = SearchResultDTO.from(info);

            // Filter out playlists and channels - only keep videos
            List<SearchItemDTO> videoItems = filterVideosOnly(dto.getItems());

            // Deduplicate items by URL
            List<SearchItemDTO> uniqueItems = deduplicateByUrl(videoItems);
            dto.setItems(uniqueItems);

            logger.info("Search completed. Found {} unique results out of {} total",
                    uniqueItems.size(), dto.getItems().size());

            return dto;
        } catch (Exception e) {
            logger.error("Failed to perform search for: {}", searchString, e);
            throw new ExtractionException("Failed to retrieve search results", e);
        }
    }

    /**
     * Retrieves the next page of search results with deduplication.
     *
     * @param searchString The original query string for the search
     * @param contentFilters List of filters to refine search results
     * @param sortFilter Sorting method for the search results
     * @param pageUrl URL representing the specific page of results to retrieve
     * @return SearchPageDTO containing the next page of search results
     * @throws ExtractionException if page extraction fails
     */
    public SearchPageDTO getSearchPage(
            String searchString,
            List<String> contentFilters,
            String sortFilter,
            String pageUrl
    ) {
        try {
            logger.info("Retrieving search page for: {} with pageUrl: {}", searchString, pageUrl);

            StreamingService service = NewPipe.getService(YOUTUBE_SERVICE_ID);
            Page pageInstance = new Page(pageUrl);

            ListExtractor.InfoItemsPage<?> page = SearchInfo.getMoreItems(
                    service,
                    service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter),
                    pageInstance
            );

            SearchPageDTO dto = SearchPageDTO.from(page);

            // Filter out playlists and channels - only keep videos
            List<SearchItemDTO> videoItems = filterVideosOnly(dto.getItems());

            // Deduplicate items by URL
            List<SearchItemDTO> uniqueItems = deduplicateByUrl(videoItems);
            dto.setItems(uniqueItems);
            dto.setItemCount(uniqueItems.size());

            logger.info("Retrieved page with {} unique results out of {} total",
                    uniqueItems.size(), page.getItems().size());

            return dto;
        } catch (Exception e) {
            logger.error("Failed to retrieve search page for: {} with pageUrl: {}", searchString, pageUrl, e);
            throw new ExtractionException("Failed to retrieve search page", e);
        }
    }

    /**
     * Filters search items to only include videos (StreamInfoItem type).
     * Excludes playlists and channels which can cause duplicate ID issues in the frontend.
     *
     * @param items List of all search items
     * @return List containing only video items
     */
    private List<SearchItemDTO> filterVideosOnly(List<SearchItemDTO> items) {
        return items.stream()
                .filter(item -> "stream".equalsIgnoreCase(item.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Deduplicates search items by URL, keeping the first occurrence.
     *
     * @param items List of search items to deduplicate
     * @return Deduplicated list of search items
     */
    private List<SearchItemDTO> deduplicateByUrl(List<SearchItemDTO> items) {
        return items.stream()
                .collect(Collectors.toMap(
                        SearchItemDTO::getUrl,
                        item -> item,
                        (existing, replacement) -> existing, // Keep first occurrence
                        LinkedHashMap::new // Preserve order
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
    }
}
