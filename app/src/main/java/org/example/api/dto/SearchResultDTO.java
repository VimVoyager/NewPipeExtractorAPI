package org.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.schabi.newpipe.extractor.search.SearchInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for search results.
 * Maps SearchInfo from NewPipe extractor to a serializable format.
 */
public class SearchResultDTO {

    @JsonProperty("url")
    private String url;

    @JsonProperty("originalUrl")
    private String originalUrl;

    @JsonProperty("name")
    private String name;

    @JsonProperty("searchString")
    private String searchString;

    @JsonProperty("searchSuggestion")
    private String searchSuggestion;

    @JsonProperty("isCorrectedSearch")
    private boolean isCorrectedSearch;

    @JsonProperty("items")
    private List<SearchItemDTO> items;

    @JsonProperty("nextPageUrl")
    private String nextPageUrl;

    @JsonProperty("hasNextPage")
    private boolean hasNextPage;

    // Constructors
    public SearchResultDTO() {}

    /**
     * Creates a SearchResultDTO from a SearchInfo object.
     *
     * @param searchInfo The SearchInfo object from NewPipe extractor
     * @return A new SearchResultDTO with mapped data
     */
    public static SearchResultDTO from(SearchInfo searchInfo) {
        SearchResultDTO dto = new SearchResultDTO();

        dto.setUrl(searchInfo.getUrl());
        dto.setOriginalUrl(searchInfo.getOriginalUrl());
        dto.setName(searchInfo.getName());
        dto.setSearchString(searchInfo.getSearchString());
        dto.setSearchSuggestion(searchInfo.getSearchSuggestion());
        dto.setCorrectedSearch(searchInfo.isCorrectedSearch());

        // Map related items to DTOs
        dto.setItems(searchInfo.getRelatedItems().stream()
                .map(SearchItemDTO::from)
                .collect(Collectors.toList()));

        // Handle pagination
        if (searchInfo.getNextPage() != null) {
            dto.setNextPageUrl(searchInfo.getNextPage().getUrl());
            dto.setHasNextPage(true);
        } else {
            dto.setHasNextPage(false);
        }

        return dto;
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchSuggestion() {
        return searchSuggestion;
    }

    public void setSearchSuggestion(String searchSuggestion) {
        this.searchSuggestion = searchSuggestion;
    }

    public boolean isCorrectedSearch() {
        return isCorrectedSearch;
    }

    public void setCorrectedSearch(boolean correctedSearch) {
        isCorrectedSearch = correctedSearch;
    }

    public List<SearchItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SearchItemDTO> items) {
        this.items = items;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public void setNextPageUrl(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }
}