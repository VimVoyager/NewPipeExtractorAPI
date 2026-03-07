package org.example.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.dto.search.SearchItemDTO;
import org.example.api.dto.search.SearchPageDTO;
import org.example.api.dto.search.SearchResultDTO;
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

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Page mockPage(String url, String id) {
        Page page = mock(Page.class);
        when(page.getUrl()).thenReturn(url);
        when(page.getId()).thenReturn(id);
        return page;
    }

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

    // ─────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from InfoItemsPage with next page")
        @SuppressWarnings("unchecked")
        void testFrom_WithNextPage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(5);
            String token = "4qmFsgJcEBIYdmlkZW8continuation";
            Page nextPage = mockPage("https://youtube.com/search?q=test&page=2", token);

            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.getNextPage()).thenReturn(nextPage);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(5, dto.getItemCount());
            assertEquals(5, dto.getItems().size());
            assertTrue(dto.isHasNextPage());
            assertNotNull(dto.getNextPage());
            assertEquals("https://youtube.com/search?q=test&page=2", dto.getNextPage().url());
            assertEquals(token, dto.getNextPage().id());
        }

        @Test
        @DisplayName("Should create DTO from InfoItemsPage without next page")
        @SuppressWarnings("unchecked")
        void testFrom_WithoutNextPage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(3);

            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.getNextPage()).thenReturn(null);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(3, dto.getItemCount());
            assertFalse(dto.isHasNextPage());
            assertNull(dto.getNextPage());
        }

        @Test
        @DisplayName("Should create DTO from empty page")
        @SuppressWarnings("unchecked")
        void testFrom_EmptyPage() {
            // Arrange
            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(List.of());
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
        @SuppressWarnings("unchecked")
        void testFrom_ItemMapping() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(2);

            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.getNextPage()).thenReturn(null);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(2, dto.getItems().size());
            assertEquals("stream", dto.getItems().get(0).getType());
            assertEquals("stream", dto.getItems().get(1).getType());
        }

        @Test
        @DisplayName("Should set id to null when Page has no id")
        @SuppressWarnings("unchecked")
        void testFrom_NullBody() {
            // Arrange
            Page nextPage = mockPage("https://youtube.com/browse", (String) null);

            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(List.of());
            when(page.getNextPage()).thenReturn(nextPage);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertNotNull(dto.getNextPage());
            assertEquals("https://youtube.com/browse", dto.getNextPage().url());
            assertNull(dto.getNextPage().id());
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should handle first page with next page available")
        @SuppressWarnings("unchecked")
        void testFirstPage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(20);
            Page nextPage = mockPage("https://youtube.com/search?page=2", "token");

            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.getNextPage()).thenReturn(nextPage);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(20, dto.getItemCount());
            assertTrue(dto.isHasNextPage());
            assertNotNull(dto.getNextPage());
            assertEquals("https://youtube.com/search?page=2", dto.getNextPage().url());
        }

        @Test
        @DisplayName("Should handle last page without next page")
        @SuppressWarnings("unchecked")
        void testLastPage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(10);

            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.getNextPage()).thenReturn(null);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(10, dto.getItemCount());
            assertFalse(dto.isHasNextPage());
            assertNull(dto.getNextPage());
        }

        @Test
        @DisplayName("Should handle middle page with next page")
        @SuppressWarnings("unchecked")
        void testMiddlePage() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(20);
            Page nextPage = mockPage("https://youtube.com/search?page=5", "tok");

            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.getNextPage()).thenReturn(nextPage);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertTrue(dto.isHasNextPage());
            assertTrue(dto.getNextPage().url().contains("page=5"));
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize nextPage as object with url and id fields")
        void testSerialize_NextPageObject() throws Exception {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();
            dto.setItems(List.of());
            dto.setItemCount(0);
            dto.setHasNextPage(true);
            dto.setNextPage(new SearchResultDTO.PageDto("https://next-page.com", "bodyBase64=="));

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"nextPage\":{"));
            assertTrue(json.contains("\"url\""));
            assertTrue(json.contains("\"id\""));
            assertTrue(json.contains("https://next-page.com"));
            assertTrue(json.contains("\"itemCount\":0"));
        }

        @Test
        @DisplayName("Should serialize nextPage as null on last page")
        void testSerialize_NullNextPage() throws Exception {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();
            dto.setItems(List.of());
            dto.setItemCount(0);
            dto.setHasNextPage(false);
            dto.setNextPage(null);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"nextPage\":null"));
            assertTrue(json.contains("\"hasNextPage\":false"));
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTrip() throws Exception {
            // Arrange
            SearchPageDTO original = new SearchPageDTO();
            original.setItems(List.of());
            original.setItemCount(5);
            original.setHasNextPage(false);
            original.setNextPage(null);

            // Act
            String json = objectMapper.writeValueAsString(original);
            SearchPageDTO deserialized = objectMapper.readValue(json, SearchPageDTO.class);

            // Assert
            assertEquals(original.getItemCount(), deserialized.getItemCount());
            assertEquals(original.isHasNextPage(), deserialized.isHasNextPage());
            assertNull(deserialized.getNextPage());
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
        @DisplayName("Should get and set nextPage")
        void testNextPage() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();
            SearchResultDTO.PageDto pageDto = new SearchResultDTO.PageDto("https://next.com", "body==");

            // Act
            dto.setNextPage(pageDto);

            // Assert
            assertSame(pageDto, dto.getNextPage());
            assertEquals("https://next.com", dto.getNextPage().url());
            assertEquals("body==", dto.getNextPage().id());
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
        @SuppressWarnings("unchecked")
        void testLargeItemCount() {
            // Arrange
            List<InfoItem> items = createMockStreamItems(1000);

            ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
            when(page.getItems()).thenReturn(items);
            when(page.getNextPage()).thenReturn(null);

            // Act
            SearchPageDTO dto = SearchPageDTO.from(page);

            // Assert
            assertEquals(1000, dto.getItemCount());
            assertEquals(1000, dto.getItems().size());
        }

        @Test
        @DisplayName("Should handle null next page")
        void testNullNextPage() {
            // Arrange
            SearchPageDTO dto = new SearchPageDTO();

            // Act
            dto.setNextPage(null);
            dto.setHasNextPage(false);

            // Assert
            assertNull(dto.getNextPage());
            assertFalse(dto.isHasNextPage());
        }
    }
}