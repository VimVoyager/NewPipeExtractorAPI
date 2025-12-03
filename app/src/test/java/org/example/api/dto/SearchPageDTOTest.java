package org.example.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for SearchPageDTO.
 * Tests pagination, mapping from InfoItemsPage, and JSON serialization.
 */
@DisplayName("SearchPageDTO Tests")
class SearchPageDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from InfoItemsPage with next page")
        void testFrom_WithNextPage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(5);
            Page nextPage = new Page("https://youtube.com/search?q=test&page=2");

            @SuppressWarnings("unchecked")
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.hasNextPage()).thenReturn(true);
            when(page.getNextPage()).thenReturn(nextPage);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(5, dto.getItemCount());
            assertEquals(5, dto.getItems().size());
            assertTrue(dto.isHasNextPage());
            assertEquals("https://youtube.com/search?q=test&page=2", dto.getNextPageUrl());
        }

        @Test
        @DisplayName("Should create DTO from InfoItemsPage without next page")
        void testFrom_WithoutNextPage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(3);

            @SuppressWarnings("unchecked")
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.hasNextPage()).thenReturn(false);
            when(page.getNextPage()).thenReturn(null);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(3, dto.getItemCount());
            assertFalse(dto.isHasNextPage());
            assertNull(dto.getNextPageUrl());
        }

        @Test
        @DisplayName("Should create DTO from empty page")
        void testFrom_EmptyPage() {
            // Arrange
            @SuppressWarnings("unchecked")
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(List.of());
            when(page.hasNextPage()).thenReturn(false);
            when(page.getNextPage()).thenReturn(null);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(0, dto.getItemCount());
            assertTrue(dto.getItems().isEmpty());
            assertFalse(dto.isHasNextPage());
        }

        @Test
        @DisplayName("Should map all items correctly")
        void testFrom_ItemMapping() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(2);

            @SuppressWarnings("unchecked")
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.hasNextPage()).thenReturn(false);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(2, dto.getItems().size());
            assertEquals("stream", dto.getItems().get(0).getType());
            assertEquals("stream", dto.getItems().get(1).getType());
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should handle first page with next page available")
        void testFirstPage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(20);
            Page nextPage = new Page("https://youtube.com/search?page=2");

            @SuppressWarnings("unchecked")
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.hasNextPage()).thenReturn(true);
            when(page.getNextPage()).thenReturn(nextPage);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(20, dto.getItemCount());
            assertTrue(dto.isHasNextPage());
            assertNotNull(dto.getNextPageUrl());
        }

        @Test
        @DisplayName("Should handle last page without next page")
        void testLastPage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(10);

            @SuppressWarnings("unchecked")
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.hasNextPage()).thenReturn(false);
            when(page.getNextPage()).thenReturn(null);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(10, dto.getItemCount());
            assertFalse(dto.isHasNextPage());
            assertNull(dto.getNextPageUrl());
        }

        @Test
        @DisplayName("Should handle middle page with next page")
        void testMiddlePage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(20);
            Page nextPage = new Page("https://youtube.com/search?page=5");

            @SuppressWarnings("unchecked")
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.hasNextPage()).thenReturn(true);
            when(page.getNextPage()).thenReturn(nextPage);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertTrue(dto.isHasNextPage());
            assertTrue(dto.getNextPageUrl().contains("page=5"));
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize to JSON with next page")
        void testSerialize_WithNextPage() throws Exception {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();
            dto.setItems(List.of());
            dto.setItemCount(0);
            dto.setHasNextPage(true);
            dto.setNextPageUrl("https://next-page.com");

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"hasNextPage\":true"));
            assertTrue(json.contains("\"nextPageUrl\":\"https://next-page.com\""));
            assertTrue(json.contains("\"itemCount\":0"));
        }

        @Test
        @DisplayName("Should deserialize from JSON")
        void testDeserialize() throws Exception {
            // Arrange
            String json = """
                {
                    "items": [],
                    "nextPageUrl": "https://next.com",
                    "hasNextPage": true,
                    "itemCount": 10
                }
                """;

            // Act
            SearchPageDTO dto = objectMapper.readValue(json, SearchPageDTO.class);

            // Assert
            assertEquals(10, dto.getItemCount());
            assertTrue(dto.isHasNextPage());
            assertEquals("https://next.com", dto.getNextPageUrl());
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTrip() throws Exception {
            // Arrange
            SearchPageDTO original = new SearchPageDTO();
            original.setItems(List.of());
            original.setItemCount(5);
            original.setHasNextPage(false);
            original.setNextPageUrl(null);

            // Act
            String json = objectMapper.writeValueAsString(original);
            SearchPageDTO deserialized = objectMapper.readValue(json, SearchPageDTO.class);

            // Assert
            assertEquals(original.getItemCount(), deserialized.getItemCount());
            assertEquals(original.isHasNextPage(), deserialized.isHasNextPage());
            assertEquals(original.getNextPageUrl(), deserialized.getNextPageUrl());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set items")
        void testItems() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();
            List<SearchItemDTO> items = List.of(new SearchItemDTO(), new SearchItemDTO());

            // Act
            dto.setItems(items);

            // Assert
            assertEquals(2, dto.getItems().size());
        }

        @Test
        @DisplayName("Should get and set nextPageUrl")
        void testNextPageUrl() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();

            // Act
            dto.setNextPageUrl("https://next.com");

            // Assert
            assertEquals("https://next.com", dto.getNextPageUrl());
        }

        @Test
        @DisplayName("Should get and set hasNextPage")
        void testHasNextPage() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();

            // Act
            dto.setHasNextPage(true);

            // Assert
            assertTrue(dto.isHasNextPage());
        }

        @Test
        @DisplayName("Should get and set itemCount")
        void testItemCount() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();

            // Act
            dto.setItemCount(42);

            // Assert
            assertEquals(42, dto.getItemCount());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null items list")
        void testNullItems() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();

            // Act
            dto.setItems(null);

            // Assert
            assertNull(dto.getItems());
        }

        @Test
        @DisplayName("Should handle empty items list")
        void testEmptyItems() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();

            // Act
            dto.setItems(List.of());

            // Assert
            assertTrue(dto.getItems().isEmpty());
        }

        @Test
        @DisplayName("Should handle large item count")
        void testLargeItemCount() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(1000);

            @SuppressWarnings("unchecked")
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.hasNextPage()).thenReturn(false);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(1000, dto.getItemCount());
            assertEquals(1000, dto.getItems().size());
        }

        @Test
        @DisplayName("Should handle null next page URL")
        void testNullNextPageUrl() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();

            // Act
            dto.setNextPageUrl(null);
            dto.setHasNextPage(false);

            // Assert
            assertNull(dto.getNextPageUrl());
            assertFalse(dto.isHasNextPage());
        }
    }

    // Helper method
    private List<InfoItem> createMockStreamItems(int count) {
        List<InfoItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            StreamInfoItem item = mock(StreamInfoItem.class);
            when(item.getName()).thenReturn("Video " + i);
            when(item.getUrl()).thenReturn("https://youtube.com/watch?v=test" + i);
            when(item.getThumbnails()).thenReturn(List.of());
            items.add(item);
        }
        return items;
    }
}