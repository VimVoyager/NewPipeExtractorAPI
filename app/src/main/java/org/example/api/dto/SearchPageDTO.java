package org.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.schabi.newpipe.extractor.ListExtractor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for paginated search results.
 * Used when retrieving additional pages of search results.
 */
public class SearchPageDTO {

    @JsonProperty("items")
    private List<SearchItemDTO> items;

    @JsonProperty("nextPageUrl")
    private String nextPageUrl;

    @JsonProperty("hasNextPage")
    private boolean hasNextPage;

    @JsonProperty("itemCount")
    private int itemCount;

    // Constructors
    public SearchPageDTO() {}

    /**
     * Creates a SearchPageDTO from a ListExtractor.InfoItemsPage.
     *
     * @param page The InfoItemsPage from NewPipe extractor
     * @return A new SearchPageDTO with mapped data
     */
    public static SearchPageDTO from(ListExtractor.InfoItemsPage<?> page) {
        SearchPageDTO dto = new SearchPageDTO();

        // Map items
        dto.setItems(page.getItems().stream()
                .map(SearchItemDTO::from)
                .collect(Collectors.toList()));

        dto.setItemCount(page.getItems().size());

        // Handle pagination
        if (page.hasNextPage() && page.getNextPage() != null) {
            dto.setNextPageUrl(page.getNextPage().getUrl());
            dto.setHasNextPage(true);
        } else {
            dto.setHasNextPage(false);
        }

        return dto;
    }

    // Getters and Setters
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

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}