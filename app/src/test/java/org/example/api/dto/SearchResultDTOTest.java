package org.example.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.dto.search.SearchItemDTO;
import org.example.api.dto.search.SearchResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for SearchResultDTO.
 * Tests mapping from SearchInfo and JSON serialization.
 */
@DisplayName("SearchResultDTO Tests")
class SearchResultDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /**
     * Builds a real Page with the given url and id (continuation token string).
     * Mirrors what YoutubeSearchExtractor.getNextPageFrom() produces:
     *   return new Page(url, token)  →  Page(String url, String id)
     * Page's methods are final, so it cannot be mocked — use real instances.
     */
    private Page realPage(String url, String id) {
        return new Page(url, id);
    }

    private InfoItem mockStreamItem(String name, String url) {
        return new org.schabi.newpipe.extractor.stream.StreamInfoItem(
                0, url, name,
                org.schabi.newpipe.extractor.stream.StreamType.VIDEO_STREAM) {
            @Override
            public java.util.List<org.schabi.newpipe.extractor.Image> getThumbnails() {
                return List.of();
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from SearchInfo with all fields")
        void testFrom_AllFields() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results?search_query=test");
            when(searchInfo.getOriginalUrl()).thenReturn("https://youtube.com/search?q=test");
            when(searchInfo.getName()).thenReturn("Search Results");
            when(searchInfo.getSearchString()).thenReturn("test");
            when(searchInfo.getSearchSuggestion()).thenReturn("test suggestion");
            when(searchInfo.isCorrectedSearch()).thenReturn(false);
            when(searchInfo.getRelatedItems()).thenReturn(List.of(mockStreamItem("Video 1", "https://youtube.com/watch?v=1")));

            String token = "4qmFsgJcEBIYdmlkZW8";
            when(searchInfo.getNextPage()).thenReturn(realPage("https://youtube.com/results?page=2", token));

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals("https://youtube.com/results?search_query=test", dto.getUrl());
            assertEquals("https://youtube.com/search?q=test", dto.getOriginalUrl());
            assertEquals("Search Results", dto.getName());
            assertEquals("test", dto.getSearchString());
            assertEquals("test suggestion", dto.getSearchSuggestion());
            assertFalse(dto.isCorrectedSearch());
            assertEquals(1, dto.getItems().size());
            assertTrue(dto.isHasNextPage());
            assertNotNull(dto.getNextPage());
            assertEquals("https://youtube.com/results?page=2", dto.getNextPage().url());
            assertEquals(token, dto.getNextPage().id());
        }

        @Test
        @DisplayName("Should create DTO from SearchInfo with minimal fields")
        void testFrom_MinimalFields() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getOriginalUrl()).thenReturn(null);
            when(searchInfo.getName()).thenReturn("Results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getSearchSuggestion()).thenReturn(null);
            when(searchInfo.isCorrectedSearch()).thenReturn(false);
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals("https://youtube.com/results", dto.getUrl());
            assertNull(dto.getOriginalUrl());
            assertEquals("Results", dto.getName());
            assertEquals("query", dto.getSearchString());
            assertNull(dto.getSearchSuggestion());
            assertFalse(dto.isCorrectedSearch());
            assertTrue(dto.getItems().isEmpty());
            assertFalse(dto.isHasNextPage());
            assertNull(dto.getNextPage());
        }

        @Test
        @DisplayName("Should handle corrected search")
        void testFrom_CorrectedSearch() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("corrected query");
            when(searchInfo.getSearchSuggestion()).thenReturn("original query");
            when(searchInfo.isCorrectedSearch()).thenReturn(true);
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertTrue(dto.isCorrectedSearch());
            assertEquals("corrected query", dto.getSearchString());
            assertEquals("original query", dto.getSearchSuggestion());
        }

        @Test
        @DisplayName("Should handle no results")
        void testFrom_NoResults() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("no results query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertTrue(dto.getItems().isEmpty());
            assertFalse(dto.isHasNextPage());
        }

        @Test
        @DisplayName("Should map multiple result items")
        void testFrom_MultipleResults() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("popular query");

            List<InfoItem> items = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                items.add(mockStreamItem("Video " + i, "https://youtube.com/watch?v=" + i));
            }
            when(searchInfo.getRelatedItems()).thenReturn(items);
            when(searchInfo.getNextPage()).thenReturn(realPage("https://youtube.com/results?page=2", "someToken"));

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals(20, dto.getItems().size());
            assertTrue(dto.isHasNextPage());
        }

        @Test
        @DisplayName("Should handle last page of results")
        void testFrom_LastPage() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results?page=5");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of(mockStreamItem("Last Video", "https://youtube.com/watch?v=last")));
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals(1, dto.getItems().size());
            assertFalse(dto.isHasNextPage());
            assertNull(dto.getNextPage());
        }
    }

    @Nested
    @DisplayName("PageDto Serialization Tests")
    class PageDtoTests {

        @Test
        @DisplayName("Should set id to null when Page has no id")
        void testBuildNextPage_NullId() {
            // Arrange — Page with url but null id (token not present)
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(realPage("https://youtube.com/browse", null));

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertNotNull(dto.getNextPage());
            assertEquals("https://youtube.com/browse", dto.getNextPage().url());
            assertNull(dto.getNextPage().id());
        }

        @Test
        @DisplayName("Should pass continuation token string through as id")
        void testBuildNextPage_TokenPassthrough() {
            // Arrange — YoutubeSearchExtractor stores the token as page.getId(), not page.getBody()
            String token = "4qmFsgJcEBIYdmlkZW8_continuation_token";

            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(realPage("https://youtube.com/browse", token));

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals(token, dto.getNextPage().id());
        }

        @Test
        @DisplayName("Should return null nextPage when Page url is null")
        void testBuildNextPage_NullUrl() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(realPage(null, "someToken"));

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertNull(dto.getNextPage());
            assertFalse(dto.isHasNextPage());
        }
    }

    @Nested
    @DisplayName("Search Suggestion Tests")
    class SearchSuggestionTests {

        @Test
        @DisplayName("Should include search suggestion when provided")
        void testSearchSuggestion() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("java programming");
            when(searchInfo.getSearchSuggestion()).thenReturn("java programming tutorial");
            when(searchInfo.isCorrectedSearch()).thenReturn(false);
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals("java programming tutorial", dto.getSearchSuggestion());
        }

        @Test
        @DisplayName("Should handle null search suggestion")
        void testNullSearchSuggestion() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getSearchSuggestion()).thenReturn(null);
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertNull(dto.getSearchSuggestion());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize nextPage as object with url and id fields")
        void testSerialize_NextPageObject() throws Exception {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(realPage("https://youtube.com/browse", "token123"));

            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert — nextPage should be a JSON object with url and id fields
            assertTrue(json.contains("\"nextPage\":{"));
            assertTrue(json.contains("\"url\""));
            assertTrue(json.contains("\"id\""));
            assertTrue(json.contains("https://youtube.com/browse"));
        }

        @Test
        @DisplayName("Should serialize nextPage as null when no further pages")
        void testSerialize_NullNextPage() throws Exception {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"nextPage\":null"));
        }

        @Test
        @DisplayName("Should serialize full DTO to JSON")
        void testSerialize_FullDto() throws Exception {
            // Arrange
            SearchResultDTO dto = new SearchResultDTO();
            dto.setSearchString("test");
            dto.setItems(List.of());
            dto.setHasNextPage(false);
            dto.setNextPage(null);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"searchString\""));
            assertTrue(json.contains("\"items\""));
            assertTrue(json.contains("\"hasNextPage\""));
            assertTrue(json.contains("test"));
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set all fields")
        void testAllFields() {
            // Arrange
            SearchResultDTO dto = new SearchResultDTO();
            List<SearchItemDTO> items = List.of(new SearchItemDTO());
            SearchResultDTO.PageDto pageDto = new SearchResultDTO.PageDto("https://next.com", "continuationToken");

            // Act
            dto.setUrl("https://url.com");
            dto.setOriginalUrl("https://original.com");
            dto.setName("Results");
            dto.setSearchString("query");
            dto.setSearchSuggestion("suggestion");
            dto.setCorrectedSearch(true);
            dto.setItems(items);
            dto.setNextPage(pageDto);
            dto.setHasNextPage(true);

            // Assert
            assertEquals("https://url.com", dto.getUrl());
            assertEquals("https://original.com", dto.getOriginalUrl());
            assertEquals("Results", dto.getName());
            assertEquals("query", dto.getSearchString());
            assertEquals("suggestion", dto.getSearchSuggestion());
            assertTrue(dto.isCorrectedSearch());
            assertEquals(1, dto.getItems().size());
            assertSame(pageDto, dto.getNextPage());
            assertEquals("https://next.com", dto.getNextPage().url());
            assertEquals("continuationToken", dto.getNextPage().id());
            assertTrue(dto.isHasNextPage());
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            // Arrange
            SearchResultDTO dto = new SearchResultDTO();

            // Act
            dto.setUrl(null);
            dto.setOriginalUrl(null);
            dto.setName(null);
            dto.setSearchString(null);
            dto.setSearchSuggestion(null);
            dto.setItems(null);
            dto.setNextPage(null);

            // Assert
            assertNull(dto.getUrl());
            assertNull(dto.getOriginalUrl());
            assertNull(dto.getName());
            assertNull(dto.getSearchString());
            assertNull(dto.getSearchSuggestion());
            assertNull(dto.getItems());
            assertNull(dto.getNextPage());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty search string")
        void testEmptySearchString() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals("", dto.getSearchString());
        }

        @Test
        @DisplayName("Should handle very long search string")
        void testLongSearchString() {
            // Arrange
            String longQuery = "a".repeat(500);
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn(longQuery);
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals(500, dto.getSearchString().length());
            assertEquals(longQuery, dto.getSearchString());
        }

        @Test
        @DisplayName("Should handle special characters in search string")
        void testSpecialCharacters() {
            // Arrange
            String specialQuery = "hello & goodbye <test> \"quotes\" 'single'";
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn(specialQuery);
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals(specialQuery, dto.getSearchString());
        }

        @Test
        @DisplayName("Should handle empty items list")
        void testEmptyItems() {
            // Arrange
            SearchResultDTO dto = new SearchResultDTO();

            // Act
            dto.setItems(List.of());

            // Assert
            assertTrue(dto.getItems().isEmpty());
        }

        @Test
        @DisplayName("Should handle large number of results")
        void testLargeResultSet() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("popular");

            List<InfoItem> items = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                items.add(mockStreamItem("Video " + i, "https://youtube.com/watch?v=" + i));
            }
            when(searchInfo.getRelatedItems()).thenReturn(items);
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals(100, dto.getItems().size());
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should indicate has next page when next page exists")
        void testHasNextPage() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(realPage("https://youtube.com/page2", "tokenABC"));

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertTrue(dto.isHasNextPage());
            assertNotNull(dto.getNextPage());
            assertEquals("https://youtube.com/page2", dto.getNextPage().url());
        }

        @Test
        @DisplayName("Should indicate no next page when at end")
        void testNoNextPage() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("query");
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertFalse(dto.isHasNextPage());
            assertNull(dto.getNextPage());
        }
    }
}