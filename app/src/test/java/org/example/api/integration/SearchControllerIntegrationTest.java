package org.example.api.integration;

import org.example.api.dto.SearchItemDTO;
import org.example.api.dto.SearchPageDTO;
import org.example.api.dto.SearchResultDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:%d/api/v1/search".formatted(port);
    }

    @Test
    @DisplayName("Search with valid query should return video results")
    public void search_withValidQuery_shouldReturnVideoResults() {
        String url = "%s?searchString=java tutorial".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        SearchResultDTO result = response.getBody();
        assertThat(result.getSearchString()).isEqualTo("java tutorial");
        assertThat(result.getItems().isEmpty()).isFalse();
        assertThat(result.getSearchSuggestion()).isNotNull();

//         Verify all items are videos (not playlists or channels)
        assertThat(result.getItems())
                .allMatch(item -> "stream".equalsIgnoreCase(item.getType()),
                        "All items should be videos (stream type)");
    }

    @Test
    @DisplayName("Search results should contain only videos, no playlists or channels")
    void search_shouldFilterOutNonVideoContent() {
        String url = "%s?searchString=music".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SearchResultDTO result = response.getBody();

        assertThat(result != null ? result.getItems() : null)
                .isNotEmpty()
                .allSatisfy(item -> {
                    assertThat(item.getType()).isEqualToIgnoringCase("stream");
                    assertThat(item.getUrl()).isNotNull().contains("/watch?v=");
                });
    }

    @Test
    @DisplayName("Search results should be deduplicated by URL")
    void search_shouldDeduplicateResults() {
        String url = "%s?searchString=popular song".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SearchResultDTO result = response.getBody();

        List<SearchItemDTO> items = result != null ? result.getItems() : null;

        // Check that all URLs are unique
        long uniqueUrls = items != null ? items.stream()
                .map(SearchItemDTO::getUrl)
                .distinct()
                .count() : 0;

        assertThat(uniqueUrls).isEqualTo(items != null ? items.size() : 0);
    }

    @Test
    @DisplayName("Search with sort filter should apply sorting")
    void search_withSortFilter_shouldApplySorting() {
        String url = "%s?searchString=tutorial&sortFilter=upload_date".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItems()).isNotEmpty();
    }

    @Test
    @DisplayName("Search with content filters should apply filters")
    void search_withContentFilters_shouldApplyFilters() {
        String url = "%s?searchString=programming&contentFilters=videos".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItems()).isNotEmpty();
    }

    @Test
    @DisplayName("Search with multiple content filters should parse comma-separated values")
    void search_withMultipleContentFilters_shouldParseCorrectly() {
        String url = getBaseUrl() + "?searchString=music&contentFilters=videos,hd";

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("Search without searchString parameter should return 400 Bad Request")
    void search_withoutSearchString_shouldReturnBadRequest() {
        String url = getBaseUrl();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Search with empty searchString should return 400 Bad Request")
    void search_withEmptySearchString_shouldReturnBadRequest() {
        String url = "%s?searchString=".formatted(getBaseUrl());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Search results should have valid structure")
    void search_shouldReturnValidStructure() {
        String url = "%s?searchString=spring boot".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SearchResultDTO result = response.getBody();

        assertThat(result)
                .isNotNull()
                .satisfies(r -> {
                    assertThat(r.getSearchString()).isNotBlank();
                    assertThat(r.getItems()).isNotNull();
//                    assertThat(r.getNextPage()).isNotNull();
                });

        // Verify each item has required fields
        assertThat(result.getItems())
                .isNotEmpty()
                .allSatisfy(item -> {
                    assertThat(item.getName()).isNotBlank();
                    assertThat(item.getUrl()).isNotBlank();
                    assertThat(item.getType()).isNotBlank();
                    assertThat(item.getDuration()).isGreaterThanOrEqualTo(-1);
                    assertThat(item.getViewCount()).isGreaterThanOrEqualTo(-1);
                });
    }

//    @Test
//    @DisplayName("Search page with valid parameters should return paginated results")
//    void searchPage_withValidParameters_shouldReturnResults() {
//        // First, get initial search results to obtain nextPage URL
//        String searchUrl = "%s?searchString=coding".formatted(getBaseUrl());
//        ResponseEntity<SearchResultDTO> searchResponse = restTemplate.exchange(
//                searchUrl,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<SearchResultDTO>() {}
//        );
//
//        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
//        String nextPageUrl = searchResponse.getBody().getNextPage().getUrl();
//        assertThat(nextPageUrl).isNotBlank();
//
//        // Now request the next page
//        String pageUrl = "%s/page?searchString=coding&pageUrl=%s".formatted(getBaseUrl(), java.net.URLEncoder.encode(nextPageUrl, java.nio.charset.StandardCharsets.UTF_8));
//
//        ResponseEntity<SearchPageDTO> pageResponse = restTemplate.exchange(
//                pageUrl,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<SearchPageDTO>() {}
//        );
//
//        assertThat(pageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
//        SearchPageDTO page = pageResponse.getBody();
//
//        assertThat(page).isNotNull();
//        assertThat(page.getItems()).isNotEmpty();
//        assertThat(page.getItemCount()).isEqualTo(page.getItems().size());
//
//        // Verify all items are videos
//        assertThat(page.getItems())
//                .allMatch(item -> "stream".equalsIgnoreCase(item.getType()));
//    }

//    @Test
//    @DisplayName("Search page results should be deduplicated")
//    void searchPage_shouldDeduplicateResults() {
//        // Get initial results
//        String searchUrl = getBaseUrl() + "?searchString=tutorial";
//        ResponseEntity<SearchResultDTO> searchResponse = restTemplate.exchange(
//                searchUrl,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<SearchResultDTO>() {}
//        );
//
//        String nextPageUrl = searchResponse.getBody().getNextPage().getUrl();
//
//        // Request next page
//        String pageUrl = getBaseUrl() + "/page?searchString=tutorial&pageUrl=" +
//                java.net.URLEncoder.encode(nextPageUrl, java.nio.charset.StandardCharsets.UTF_8);
//
//        ResponseEntity<SearchPageDTO> pageResponse = restTemplate.exchange(
//                pageUrl,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<SearchPageDTO>() {}
//        );
//
//        SearchPageDTO page = pageResponse.getBody();
//        List<SearchItemDTO> items = page.getItems();
//
//        // Verify no duplicate URLs
//        long uniqueUrls = items.stream()
//                .map(SearchItemDTO::getUrl)
//                .distinct()
//                .count();
//
//        assertThat(uniqueUrls).isEqualTo(items.size());
//    }
//
//    @Test
//    @DisplayName("Search page without searchString should return 400")
//    void searchPage_withoutSearchString_shouldReturnBadRequest() {
//        String url = getBaseUrl() + "/page?pageUrl=somePageUrl";
//
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    @DisplayName("Search page without pageUrl should return 400")
//    void searchPage_withoutPageUrl_shouldReturnBadRequest() {
//        String url = getBaseUrl() + "/page?searchString=test";
//
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    @DisplayName("Search page with empty pageUrl should return 400")
//    void searchPage_withEmptyPageUrl_shouldReturnBadRequest() {
//        String url = getBaseUrl() + "/page?searchString=test&pageUrl=";
//
//        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<Map<String, Object>>() {}
//        );
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//    }
//
//    @Test
//    @DisplayName("Search page with invalid pageUrl should return error")
//    void searchPage_withInvalidPageUrl_shouldReturnError() {
//        String url = getBaseUrl() + "/page?searchString=test&pageUrl=invalid_page_url";
//
//        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<Map<String, Object>>() {}
//        );
//
//        // Should return either 400 or 500 depending on error handling
//        assertThat(response.getStatusCode())
//                .isIn(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @Test
    @DisplayName("Search should handle special characters in query")
    void search_withSpecialCharacters_shouldHandleCorrectly() {
        String url = "%s?searchString=C%%2B%%2B+programming".formatted(getBaseUrl()); // "C++ programming"

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getItems()).isNotEmpty();
    }

    @Test
    @DisplayName("Multiple concurrent search requests should be handled safely")
    void search_concurrentRequests_shouldHandleSafely() throws InterruptedException {
        String url = "%s?searchString=test".formatted(getBaseUrl());
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        java.util.concurrent.atomic.AtomicInteger successCount =
                new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<SearchResultDTO>() {}
                );
                if (response.getStatusCode() == HttpStatus.OK) {
                    successCount.incrementAndGet();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join(10000);
        }

        assertThat(successCount.get()).isEqualTo(threadCount);
    }
}
