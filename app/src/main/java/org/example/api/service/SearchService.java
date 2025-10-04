package org.example.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.utils.PaginationUtils;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.example.api.utils.ErrorUtils.getError;

@Service
public class SearchService {
    private final ObjectMapper objectMapper;
    private final PaginationUtils paginationUtils;

    public SearchService(ObjectMapper objectMapper, PaginationUtils paginationUtils) {
        this.objectMapper = objectMapper;
        this.paginationUtils = paginationUtils;
    }

    public String getSearchInfo(int serviceId, String searchString, List<String> contentFilters, String sortFilter) throws Exception {
        try {
            StreamingService service = NewPipe.getService(serviceId);
            SearchInfo info = SearchInfo.getInfo(service, service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter));
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            System.err.println("Search Info Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getSearchPage(int serviceId, String searchString, List<String> contentFilters, String sortFilter, String pageUrl) throws Exception {
        return paginationUtils.handlePaginatedResponse(() -> {
            try {
                StreamingService service = NewPipe.getService(serviceId);
                Page pageInstance = new Page(pageUrl);
                return SearchInfo.getMoreItems(
                        service,
                        service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter),
                        pageInstance
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract search page", e);
            }
        });
    }
}
