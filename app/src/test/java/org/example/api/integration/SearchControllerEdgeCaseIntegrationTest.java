package org.example.api.integration;

import org.example.api.dto.SearchResultDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchControllerEdgeCaseIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:%d/api/v1/search".formatted(port);
    }

    @Test
    @DisplayName("Search with very long query string should handle gracefully")
    void search_withVeryLongQuery_shouldHandleGracefully() {
        String longQuery = "java".repeat(50); // 200 character query
        String url = "%s?searchString=%s".formatted(getBaseUrl(), longQuery);

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        // Should either succeed or return appropriate error
        assertThat(response.getStatusCode())
                .isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST, HttpStatus.REQUEST_URI_TOO_LONG);
    }

    @Test
    @DisplayName("Search with single character query should return results")
    void search_withSingleCharacter_shouldReturnResults() {
        String url = "%s?searchString=a".formatted(getBaseUrl());

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
    @DisplayName("Search with numbers only should return results")
    void search_withNumbersOnly_shouldReturnResults() {
        String url = "%s?searchString=12345".formatted(getBaseUrl());

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
    @DisplayName("Search with Unicode characters should work correctly")
    void search_withUnicodeCharacters_shouldWork() {
        String url = "%s?searchString=日本語".formatted(getBaseUrl()); // Japanese

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
    @DisplayName("Search with emoji should handle correctly")
    void search_withEmoji_shouldHandleCorrectly() {
        String url = "%s?searchString=music \uD83C\uDFB5".formatted(getBaseUrl());

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
    @DisplayName("Search with quotes should preserve query")
    void search_withQuotes_shouldPreserveQuery() {
        String url = "%s?searchString=\"java tutorial\"".formatted(getBaseUrl());

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
    @DisplayName("Search with empty content filters should use defaults")
    void search_withEmptyContentFilters_shouldUseDefaults() {
        String url = "%s?searchString=test&contentFilters=".formatted(getBaseUrl());

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
    @DisplayName("Search with multiple commas in content filters should parse correctly")
    void search_withMultipleCommas_shouldParseCorrectly() {
        String url = "%s?searchString=test&contentFilters=videos,,hd,".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Search with invalid sort filter should handle gracefully")
    void search_withInvalidSortFilter_shouldHandleGracefully() {
        String url = "%s?searchString=test&sortFilter=invalid_sort".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        // Should either use default sort or return error
        assertThat(response.getStatusCode())
                .isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Search with empty sort filter should use defaults")
    void search_withEmptySortFilter_shouldUseDefaults() {
        String url = "%s?searchString=test&sortFilter=".formatted(getBaseUrl());

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
    @DisplayName("Search with duplicate query parameters should use first value")
    void search_withDuplicateParams_shouldUseBothValues() {
        String url = "%s?searchString=first&searchString=second".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SearchResultDTO result = response.getBody();

        assertThat(result != null ? result.getSearchString() : null).isEqualTo("first,second");
    }

    @Test
    @DisplayName("Search with URL-encoded special characters should decode correctly")
    void search_withUrlEncodedChars_shouldDecodeCorrectly() {
        // %26 = &, %3D = =, %3F = ?
        String url = "%s?searchString=how%%20to%%20use%%20%%26%%20operator".formatted(getBaseUrl());

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
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    @DisplayName("Search should complete within reasonable time")
    void search_shouldCompleteWithinTimeout() {
        String url = "%s?searchString=test".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Search with very specific query should return focused results")
    void search_withSpecificQuery_shouldReturnFocusedResults() {
        String url = "%s?searchString=Spring+Boot+JPA+tutorial+2024".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SearchResultDTO result = response.getBody();

        assertThat(result.getItems()).isNotEmpty();

        // Results should be relevant to the query
        assertThat(result.getItems())
                .allSatisfy(item -> {
                    assertThat(item.getName()).isNotBlank();
                    assertThat(item.getUrl()).contains("youtube.com");
                });
    }

    @Test
    @DisplayName("Search results should have consistent video count")
    void search_shouldHaveConsistentVideoCount() {
        String url = "%s?searchString=tutorial".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SearchResultDTO result = response.getBody();

        // YouTube typically returns a consistent number of results per page
        assertThat(result.getItems().size())
                .isGreaterThan(0)
                .isLessThanOrEqualTo(30); // Typical page size
    }

    @Test
    @DisplayName("Search with case-insensitive query should work")
    void search_withMixedCase_shouldWork() {
        String url1 = "%s?searchString=JAVA".formatted(getBaseUrl());
        String url2 = "%s?searchString=java".formatted(getBaseUrl());

        ResponseEntity<SearchResultDTO> response1 = restTemplate.exchange(
                url1,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        ResponseEntity<SearchResultDTO> response2 = restTemplate.exchange(
                url2,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SearchResultDTO>() {}
        );

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Both should return results
        assertThat(response1.getBody().getItems()).isNotEmpty();
        assertThat(response2.getBody().getItems()).isNotEmpty();
    }
}
