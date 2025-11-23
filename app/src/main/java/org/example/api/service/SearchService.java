package org.example.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.dto.SearchPageDTO;
import org.example.api.dto.SearchResultDTO;
import org.example.api.utils.PaginationUtils;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.example.api.utils.ErrorUtils.getError;

/**
 * Service for performing and managing search operations across
 * different streaming services using NewPipe extractor.
 *
 * This service provides methods to retrieve initial search results
 * and paginated search results from various streaming platforms,
 * with robust error handling and flexible filtering options.
 */
@Service
public class SearchService {
    private static final int YOUTUBE_SERVICE_ID = 0;
    private final ObjectMapper objectMapper;
    private final PaginationUtils paginationUtils;

    /**
     * Constructor to inject dependencies for search operations.
     *
     * @param objectMapper The ObjectMapper for JSON processing
     * @param paginationUtils Utility for handling paginated responses
     */
    public SearchService(ObjectMapper objectMapper, PaginationUtils paginationUtils) {
        this.objectMapper = objectMapper;
        this.paginationUtils = paginationUtils;
    }

    /**
     * Performs an initial search across a specified streaming service.
     *
     * Retrieves search results based on the provided service ID,
     * search string, content filters, and sorting preferences.
     * Handles potential exceptions and returns either the search
     * results or an error response.
     *
     * @param searchString The query string for the search
     * @param contentFilters List of filters to refine search results
     * @param sortFilter Sorting method for the search results
     * @return A JSON string containing search information or an error message
     * @throws Exception If an error occurs during the search process
     */
    public String getSearchInfo(String searchString, List<String> contentFilters, String sortFilter) throws Exception {
        try {
            StreamingService service = NewPipe.getService(YOUTUBE_SERVICE_ID);
            SearchInfo info = SearchInfo.getInfo(service, service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter));
            SearchResultDTO dto = SearchResultDTO.from(info);
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            System.err.println("Search Info Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    /**
     * Retrieves the next page of search results for a given search.
     *
     * Utilizes pagination utilities to fetch additional search results
     * based on the initial search parameters and a specific page URL.
     * Supports continued loading of search results across multiple pages.
     *
     * @param searchString The original query string for the search
     * @param contentFilters List of filters to refine search results
     * @param sortFilter Sorting method for the search results
     * @param pageUrl URL representing the specific page of results to retrieve
     * @return A JSON string containing the next page of search results or an error message
     * @throws Exception If an error occurs during page retrieval
     */
    public String getSearchPage(String searchString, List<String> contentFilters, String sortFilter, String pageUrl) throws Exception {
        return paginationUtils.handlePaginatedResponse(() -> {
            try {
                StreamingService service = NewPipe.getService(YOUTUBE_SERVICE_ID);
                Page pageInstance = new Page(pageUrl);
                ListExtractor.InfoItemsPage<?> page = SearchInfo.getMoreItems(
                        service,
                        service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter),
                        pageInstance
                );

                return SearchPageDTO.from(page);
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract search page", e);
            }
        });
    }
}
