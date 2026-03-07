package org.example.api.dto.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.schabi.newpipe.extractor.ListExtractor;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for paginated search results.
 * Used when retrieving additional pages of search results.
 */
public class SearchPageDTO {

    @JsonProperty("items")
    private List<SearchItemDTO> items;

    @JsonProperty("nextPage")
    private SearchResultDTO.PageDto nextPage;

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

        dto.setItems(page.getItems().stream()
                .map(SearchItemDTO::from)
                .collect(Collectors.toList()));

        dto.setItemCount(page.getItems().size());
        dto.setNextPage(buildNextPage(page.getNextPage()));
        dto.setHasNextPage(dto.nextPage != null);

        return dto;
    }

    // Helpers
    private static SearchResultDTO.PageDto buildNextPage(org.schabi.newpipe.extractor.Page page) {
        if (page == null || page.getUrl() == null) return null;
        return new SearchResultDTO.PageDto(page.getUrl(), page.getId());
    }

    // Getters and Setters
    public List<SearchItemDTO> getItems() { return items; }
    public void setItems(List<SearchItemDTO> items) { this.items = items; }

    public SearchResultDTO.PageDto getNextPage() { return nextPage; }
    public void setNextPage(SearchResultDTO.PageDto nextPage) { this.nextPage = nextPage; }

    public boolean isHasNextPage() { return hasNextPage; }
    public void setHasNextPage(boolean hasNextPage) { this.hasNextPage = hasNextPage; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }
}