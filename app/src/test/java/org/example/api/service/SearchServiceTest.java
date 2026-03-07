package org.example.api.service;

import org.example.api.dto.search.SearchItemDTO;
import org.example.api.dto.search.SearchPageDTO;
import org.example.api.dto.search.SearchResultDTO;
import org.example.api.exception.ExtractionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for SearchService.
 * Tests search functionality, deduplication, pagination, and error handling.
 */
@DisplayName("SearchService Tests")
class SearchServiceTest {

    private SearchService searchService;
    private StreamingService mockStreamingService;
    private SearchQueryHandlerFactory mockQueryHandlerFactory;
    private SearchExtractor mockSearchExtractor;

    @BeforeEach
    void setUp() throws Exception {
        searchService = new SearchService();
        mockStreamingService = mock(StreamingService.class);
        mockQueryHandlerFactory = mock(SearchQueryHandlerFactory.class);
        mockSearchExtractor = mock(SearchExtractor.class);

        SearchQueryHandler mockHandler = mock(SearchQueryHandler.class);
        when(mockStreamingService.getSearchQHFactory()).thenReturn(mockQueryHandlerFactory);
        when(mockQueryHandlerFactory.fromQuery(any(), any(), any())).thenReturn(mockHandler);
        when(mockStreamingService.getSearchExtractor((SearchQueryHandler) any())).thenReturn(mockSearchExtractor);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private SearchInfo mockSearchInfo(String searchString) {
        SearchInfo info = mock(SearchInfo.class);
        when(info.getSearchString()).thenReturn(searchString);
        when(info.getRelatedItems()).thenReturn(Collections.emptyList());
        when(info.getNextPage()).thenReturn(null);
        return info;
    }

    @SuppressWarnings("unchecked")
    private ListExtractor.InfoItemsPage<InfoItem> mockEmptyPage() {
        ListExtractor.InfoItemsPage<InfoItem> page = mock(ListExtractor.InfoItemsPage.class);
        when(page.getItems()).thenReturn(Collections.emptyList());
        when(page.getNextPage()).thenReturn(null);
        return page;
    }

    /**
     * Returns a minimal concrete StreamInfoItem subclass. StreamInfoItem has final
     * methods (getName, getUrl, getThumbnails) that Mockito cannot stub, so we use
     * an anonymous subclass.
     */
    private StreamInfoItem stubInfoItem(String name, String url) {
        return new StreamInfoItem(0, url, name, StreamType.VIDEO_STREAM) {
            @Override
            public java.util.List<org.schabi.newpipe.extractor.Image> getThumbnails() {
                return List.of();
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Search Info Tests")
    class SearchInfoTests {

        @Test
        @DisplayName("Should return search results successfully")
        void testGetSearchInfo_Success() throws Exception {
            // Arrange
            String searchString = "java tutorial";
            SearchInfo mockInfo = mockSearchInfo(searchString);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                // Service calls SearchInfo.getInfo(extractor) — the one-arg overload
                searchInfoMock.when(() -> SearchInfo.getInfo(any(SearchExtractor.class)))
                        .thenReturn(mockInfo);

                // Act
                SearchResultDTO result = searchService.getSearchInfo(
                        searchString, Collections.emptyList(), null);

                // Assert
                assertNotNull(result);
                assertEquals(searchString, result.getSearchString());
                assertNotNull(result.getItems());
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException with descriptive message when NewPipe fails")
        void testGetSearchInfo_ThrowsExtractionException() {
            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getService(anyInt()))
                        .thenThrow(new RuntimeException("NewPipe failure"));

                ExtractionException exception = assertThrows(ExtractionException.class, () ->
                        searchService.getSearchInfo("test", Collections.emptyList(), null)
                );

                assertTrue(exception.getMessage().contains("NewPipe failure"));
            }
        }

        @Test
        @DisplayName("Should return empty items when no results found")
        void testGetSearchInfo_EmptyResults() throws Exception {
            SearchInfo mockInfo = mockSearchInfo("no results query");

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                searchInfoMock.when(() -> SearchInfo.getInfo(any(SearchExtractor.class)))
                        .thenReturn(mockInfo);

                SearchResultDTO result = searchService.getSearchInfo(
                        "no results query", Collections.emptyList(), null);

                assertNotNull(result.getItems());
                assertTrue(result.getItems().isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("Search Page Tests")
    class SearchPageTests {

        @Test
        @DisplayName("Should return paginated results successfully")
        void testGetSearchPage_Success() throws Exception {
            // Arrange
            String pageUrl = "https://www.youtube.com/youtubei/v1/search?prettyPrint=false";
            String pageId = "4qmFsgJcEBIYdmlkZW8continuation";

            ListExtractor.InfoItemsPage<InfoItem> mockPage = mockEmptyPage();

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                // Service calls extractor.getPage(pageInstance) directly
                when(mockSearchExtractor.getPage(any(Page.class))).thenReturn(mockPage);

                SearchPageDTO result = searchService.getSearchPage(
                        "java", Collections.emptyList(), null, pageUrl, pageId);

                assertNotNull(result);
                assertNotNull(result.getItems());
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException with descriptive message when pagination fails")
        void testGetSearchPage_ThrowsExtractionException() {
            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getService(anyInt()))
                        .thenThrow(new RuntimeException("Pagination failure"));

                ExtractionException exception = assertThrows(ExtractionException.class, () ->
                        searchService.getSearchPage(
                                "test", Collections.emptyList(), null,
                                "https://youtube.com/next", "someToken")
                );

                assertTrue(exception.getMessage().contains("Pagination failure"));
            }
        }

        @Test
        @DisplayName("Should reconstruct Page with the correct id token and call extractor.getPage")
        void testGetSearchPage_ReconstructsPageWithId() throws Exception {
            // Arrange
            String pageUrl = "https://www.youtube.com/youtubei/v1/search?prettyPrint=false";
            String pageId = "4qmFsgJcEBIYdmlkZW8token";

            ListExtractor.InfoItemsPage<InfoItem> mockPage = mockEmptyPage();

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                when(mockSearchExtractor.getPage(any(Page.class))).thenReturn(mockPage);

                searchService.getSearchPage("test", Collections.emptyList(), null, pageUrl, pageId);

                // Verify extractor.getPage was called with a Page carrying the correct id
                verify(mockSearchExtractor).getPage(argThat(page ->
                        pageUrl.equals(page.getUrl()) && pageId.equals(page.getId())
                ));
            }
        }

        @Test
        @DisplayName("Should handle null pageId without throwing")
        void testGetSearchPage_NullPageId() throws Exception {
            String pageUrl = "https://www.youtube.com/youtubei/v1/search?prettyPrint=false";
            ListExtractor.InfoItemsPage<InfoItem> mockPage = mockEmptyPage();

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                when(mockSearchExtractor.getPage(any(Page.class))).thenReturn(mockPage);

                SearchPageDTO result = searchService.getSearchPage(
                        "test", Collections.emptyList(), null, pageUrl, null);

                assertNotNull(result);
            }
        }
    }

    @Nested
    @DisplayName("Deduplication Logic Tests")
    class DeduplicationTests {

        @Test
        @DisplayName("Should handle empty search results")
        void testDeduplication_EmptyResults() throws Exception {
            SearchInfo mockInfo = mockSearchInfo("test");

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                searchInfoMock.when(() -> SearchInfo.getInfo(any(SearchExtractor.class)))
                        .thenReturn(mockInfo);

                SearchResultDTO result = searchService.getSearchInfo(
                        "test", Collections.emptyList(), null);

                assertNotNull(result.getItems());
                assertTrue(result.getItems().isEmpty());
            }
        }

        @Test
        @DisplayName("Should deduplicate items with the same URL")
        void testDeduplication_RemovesDuplicateUrls() throws Exception {
            // stubInfoItem returns a StreamInfoItem subclass so SearchItemDTO.from() sets type="stream"
            InfoItem item1 = stubInfoItem("Video A", "https://youtube.com/watch?v=same");
            InfoItem item2 = stubInfoItem("Video A duplicate", "https://youtube.com/watch?v=same");
            InfoItem item3 = stubInfoItem("Video B", "https://youtube.com/watch?v=unique");

            SearchInfo mockInfo = mock(SearchInfo.class);
            when(mockInfo.getSearchString()).thenReturn("test");
            when(mockInfo.getRelatedItems()).thenReturn(List.of(item1, item2, item3));
            when(mockInfo.getNextPage()).thenReturn(null);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                searchInfoMock.when(() -> SearchInfo.getInfo(any(SearchExtractor.class)))
                        .thenReturn(mockInfo);

                SearchResultDTO result = searchService.getSearchInfo(
                        "test", Collections.emptyList(), null);

                // Items are StreamInfoItem instances so SearchItemDTO.from() gives type="stream",
                // they pass filterVideosOnly, and the duplicate URL is collapsed to one entry.
                assertEquals(2, result.getItems().size());
                long distinctUrls = result.getItems().stream()
                        .map(SearchItemDTO::getUrl)
                        .distinct()
                        .count();
                assertEquals(2, distinctUrls);
            }
        }

        @Test
        @DisplayName("Should keep first occurrence when deduplicating")
        void testDeduplication_KeepsFirstOccurrence() throws Exception {
            InfoItem first = stubInfoItem("First Video", "https://youtube.com/watch?v=dup");
            InfoItem second = stubInfoItem("Second Video (dup)", "https://youtube.com/watch?v=dup");

            SearchInfo mockInfo = mock(SearchInfo.class);
            when(mockInfo.getSearchString()).thenReturn("test");
            when(mockInfo.getRelatedItems()).thenReturn(List.of(first, second));
            when(mockInfo.getNextPage()).thenReturn(null);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                searchInfoMock.when(() -> SearchInfo.getInfo(any(SearchExtractor.class)))
                        .thenReturn(mockInfo);

                SearchResultDTO result = searchService.getSearchInfo(
                        "test", Collections.emptyList(), null);

                assertEquals(1, result.getItems().size());
                assertEquals("First Video", result.getItems().get(0).getName());
            }
        }

        @Test
        @DisplayName("Should filter out non-stream items (channels, playlists)")
        void testDeduplication_FiltersNonStreamItems() throws Exception {
            // stubInfoItem returns a StreamInfoItem so SearchItemDTO.from() sets type="stream", passing the filter
            InfoItem stream = stubInfoItem("A Video", "https://youtube.com/watch?v=vid1");

            // A non-stream item: use InfoType.CHANNEL so SearchItemDTO.from() sets type="channel"
            InfoItem nonStream = new InfoItem(InfoItem.InfoType.CHANNEL, 0,
                    "https://youtube.com/channel/UC1", "A Channel") {
                @Override
                public java.util.List<org.schabi.newpipe.extractor.Image> getThumbnails() {
                    return List.of();
                }
            };

            SearchInfo mockInfo = mock(SearchInfo.class);
            when(mockInfo.getSearchString()).thenReturn("test");
            when(mockInfo.getRelatedItems()).thenReturn(List.of(stream, nonStream));
            when(mockInfo.getNextPage()).thenReturn(null);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                searchInfoMock.when(() -> SearchInfo.getInfo(any(SearchExtractor.class)))
                        .thenReturn(mockInfo);

                SearchResultDTO result = searchService.getSearchInfo(
                        "test", Collections.emptyList(), null);

                // Only the stream item survives filterVideosOnly
                assertEquals(1, result.getItems().size());
                assertEquals("stream", result.getItems().get(0).getType());
            }
        }
    }
}