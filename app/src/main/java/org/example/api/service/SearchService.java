package org.example.api.service;

import org.example.api.dto.search.SearchPageDTO;
import org.example.api.dto.search.SearchResultDTO;
import org.example.api.dto.search.SearchItemDTO;
import org.example.api.exception.ExtractionException;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for performing and managing search operations across
 * different streaming services using NewPipe extractor.
 */
@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static final int YOUTUBE_SERVICE_ID = 0;

    /**
     * Performs an initial search and returns deduplicated results.
     */
    public SearchResultDTO getSearchInfo(
            String searchString,
            List<String> contentFilters,
            String sortFilter
    ) throws ExtractionException {
        try {
            logger.info("Performing search for: {}", searchString);

            StreamingService service = NewPipe.getService(YOUTUBE_SERVICE_ID);

            SearchExtractor extractor = service.getSearchExtractor(
                    service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter)
            );
            extractor.fetchPage();


            SearchInfo info = SearchInfo.getInfo(extractor);
            SearchResultDTO dto = SearchResultDTO.from(info);

            List<SearchItemDTO> videoItems = filterVideosOnly(dto.getItems());
            List<SearchItemDTO> uniqueItems = deduplicateByUrl(videoItems);
            dto.setItems(uniqueItems);

            logger.info("Search completed. Found {} unique results", uniqueItems.size());

            return dto;
        } catch (Exception e) {
            logger.error("Failed to retrieve search results for: {}", searchString, e);
            throw new ExtractionException("Failed to retrieve search results: " + e.getMessage(), e.getCause());
        }
    }

    /**
     * Retrieves the next page of search results.
     *
     * <p>Mirrors the pattern used in {@link ChannelTabService#getChannelTabPage}: a
     * {@link SearchExtractor} is obtained for the original query, initialised with
     * {@code fetchPage()} to establish the InnerTube session state, and then
     * {@code getPage(pageInstance)} is called with the reconstructed {@link Page}.
     * Using {@code SearchInfo.getMoreItems()} without this initialisation step skips
     * the extractor setup and results in empty pages.</p>
     *
     * @param searchString   the original query string
     * @param contentFilters list of filters
     * @param sortFilter     sort method
     * @param pageUrl        from {@code nextPage.url} in the previous response
     * @param pageId      from {@code nextPage.Id} in the previous response
     */
    public SearchPageDTO getSearchPage(
            String searchString,
            List<String> contentFilters,
            String sortFilter,
            String pageUrl,
            String pageId
    ) throws ExtractionException {
        try {
            logger.info("Retrieving search page for: {}", searchString);

            StreamingService service = NewPipe.getService(YOUTUBE_SERVICE_ID);

            SearchExtractor extractor = service.getSearchExtractor(
                    service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter)
            );
            extractor.fetchPage();

            Page pageInstance = new Page(pageUrl, pageId);

            ListExtractor.InfoItemsPage<?> page = extractor.getPage(pageInstance);

            SearchPageDTO dto = SearchPageDTO.from(page);

            List<SearchItemDTO> videoItems = filterVideosOnly(dto.getItems());
            List<SearchItemDTO> uniqueItems = deduplicateByUrl(videoItems);
            dto.setItems(uniqueItems);
            dto.setItemCount(uniqueItems.size());

            logger.info("Retrieved page with {} unique results", uniqueItems.size());

            return dto;
        } catch (Exception e) {
            logger.error("Failed to retrieve search page for: {}", searchString, e);
            throw new ExtractionException("Failed to retrieve search page: " + e.getMessage(), e.getCause());
        }
    }

    private List<SearchItemDTO> filterVideosOnly(List<SearchItemDTO> items) {
        return items.stream()
                .filter(item -> "stream".equalsIgnoreCase(item.getType()))
                .collect(Collectors.toList());
    }

    private List<SearchItemDTO> deduplicateByUrl(List<SearchItemDTO> items) {
        return new ArrayList<>(items.stream()
                .collect(Collectors.toMap(
                        SearchItemDTO::getUrl,
                        item -> item,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ))
                .values());
    }
}