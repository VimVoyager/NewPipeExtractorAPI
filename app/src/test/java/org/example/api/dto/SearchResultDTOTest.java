package org.example.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

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

            StreamInfoItem item = mock(StreamInfoItem.class);
            when(item.getName()).thenReturn("Video 1");
            when(item.getUrl()).thenReturn("https://youtube.com/watch?v=1");
            when(item.getThumbnails()).thenReturn(List.of());
            when(searchInfo.getRelatedItems()).thenReturn(List.of(item));

            Page nextPage = new Page("https://youtube.com/results?page=2");
            when(searchInfo.getNextPage()).thenReturn(nextPage);

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
            assertEquals("https://youtube.com/results?page=2", dto.getNextPageUrl());
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
            assertNull(dto.getNextPageUrl());
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
                StreamInfoItem item = mock(StreamInfoItem.class);
                when(item.getName()).thenReturn("Video " + i);
                when(item.getUrl()).thenReturn("https://youtube.com/watch?v=" + i);
                when(item.getThumbnails()).thenReturn(List.of());
                items.add(item);
            }
            when(searchInfo.getRelatedItems()).thenReturn(items);
            when(searchInfo.getNextPage()).thenReturn(new Page("https://youtube.com/results?page=2"));

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

            StreamInfoItem item = mock(StreamInfoItem.class);
            when(item.getName()).thenReturn("Last Video");
            when(item.getUrl()).thenReturn("https://youtube.com/watch?v=last");
            when(item.getThumbnails()).thenReturn(List.of());
            when(searchInfo.getRelatedItems()).thenReturn(List.of(item));
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals(1, dto.getItems().size());
            assertFalse(dto.isHasNextPage());
            assertNull(dto.getNextPageUrl());
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
            when(searchInfo.getSearchString()).thenReturn("javasript");
            when(searchInfo.getSearchSuggestion()).thenReturn("javascript"); // Typo corrected
            when(searchInfo.isCorrectedSearch()).thenReturn(false);
            when(searchInfo.getRelatedItems()).thenReturn(List.of());
            when(searchInfo.getNextPage()).thenReturn(null);

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertEquals("javasript", dto.getSearchString());
            assertEquals("javascript", dto.getSearchSuggestion());
        }

        @Test
        @DisplayName("Should handle null search suggestion")
        void testNullSearchSuggestion() {
            // Arrange
            SearchInfo searchInfo = mock(SearchInfo.class);
            when(searchInfo.getUrl()).thenReturn("https://youtube.com/results");
            when(searchInfo.getSearchString()).thenReturn("perfect query");
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
        @DisplayName("Should serialize to JSON with all fields")
        void testSerialize_AllFields() throws Exception {
            // Arrange
            SearchResultDTO dto = new SearchResultDTO();
            dto.setUrl("https://youtube.com/results");
            dto.setOriginalUrl("https://youtube.com/search");
            dto.setName("Results");
            dto.setSearchString("test query");
            dto.setSearchSuggestion("suggestion");
            dto.setCorrectedSearch(true);
            dto.setItems(List.of());
            dto.setNextPageUrl("https://youtube.com/page2");
            dto.setHasNextPage(true);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"url\":\"https://youtube.com/results\""));
            assertTrue(json.contains("\"searchString\":\"test query\""));
            assertTrue(json.contains("\"isCorrectedSearch\":true"));
            assertTrue(json.contains("\"hasNextPage\":true"));
        }

        @Test
        @DisplayName("Should deserialize from JSON")
        void testDeserialize() throws Exception {
            // Arrange
            String json = """
                {
                    "url": "https://test.com/results",
                    "originalUrl": "https://test.com/search",
                    "name": "Search Results",
                    "searchString": "test",
                    "searchSuggestion": "testing",
                    "isCorrectedSearch": false,
                    "items": [],
                    "nextPageUrl": "https://test.com/page2",
                    "hasNextPage": true
                }
                """;

            // Act
            SearchResultDTO dto = objectMapper.readValue(json, SearchResultDTO.class);

            // Assert
            assertEquals("https://test.com/results", dto.getUrl());
            assertEquals("https://test.com/search", dto.getOriginalUrl());
            assertEquals("Search Results", dto.getName());
            assertEquals("test", dto.getSearchString());
            assertEquals("testing", dto.getSearchSuggestion());
            assertFalse(dto.isCorrectedSearch());
            assertTrue(dto.getItems().isEmpty());
            assertEquals("https://test.com/page2", dto.getNextPageUrl());
            assertTrue(dto.isHasNextPage());
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTrip() throws Exception {
            // Arrange
            SearchResultDTO original = new SearchResultDTO();
            original.setUrl("https://youtube.com/results");
            original.setSearchString("round trip test");
            original.setCorrectedSearch(false);
            original.setItems(List.of());
            original.setHasNextPage(false);

            // Act
            String json = objectMapper.writeValueAsString(original);
            SearchResultDTO deserialized = objectMapper.readValue(json, SearchResultDTO.class);

            // Assert
            assertEquals(original.getUrl(), deserialized.getUrl());
            assertEquals(original.getSearchString(), deserialized.getSearchString());
            assertEquals(original.isCorrectedSearch(), deserialized.isCorrectedSearch());
            assertEquals(original.isHasNextPage(), deserialized.isHasNextPage());
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

            // Act
            dto.setUrl("https://url.com");
            dto.setOriginalUrl("https://original.com");
            dto.setName("Results");
            dto.setSearchString("query");
            dto.setSearchSuggestion("suggestion");
            dto.setCorrectedSearch(true);
            dto.setItems(items);
            dto.setNextPageUrl("https://next.com");
            dto.setHasNextPage(true);

            // Assert
            assertEquals("https://url.com", dto.getUrl());
            assertEquals("https://original.com", dto.getOriginalUrl());
            assertEquals("Results", dto.getName());
            assertEquals("query", dto.getSearchString());
            assertEquals("suggestion", dto.getSearchSuggestion());
            assertTrue(dto.isCorrectedSearch());
            assertEquals(1, dto.getItems().size());
            assertEquals("https://next.com", dto.getNextPageUrl());
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
            dto.setNextPageUrl(null);

            // Assert
            assertNull(dto.getUrl());
            assertNull(dto.getOriginalUrl());
            assertNull(dto.getName());
            assertNull(dto.getSearchString());
            assertNull(dto.getSearchSuggestion());
            assertNull(dto.getItems());
            assertNull(dto.getNextPageUrl());
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
                StreamInfoItem item = mock(StreamInfoItem.class);
                when(item.getName()).thenReturn("Video " + i);
                when(item.getUrl()).thenReturn("https://youtube.com/watch?v=" + i);
                when(item.getThumbnails()).thenReturn(List.of());
                items.add(item);
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
            when(searchInfo.getNextPage()).thenReturn(new Page("https://youtube.com/page2"));

            // Act
            SearchResultDTO dto = SearchResultDTO.from(searchInfo);

            // Assert
            assertTrue(dto.isHasNextPage());
            assertNotNull(dto.getNextPageUrl());
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
            assertNull(dto.getNextPageUrl());
        }
    }
}