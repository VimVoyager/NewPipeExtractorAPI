package org.example.api.service;

import org.example.api.dto.SearchItemDTO;
import org.example.api.dto.SearchPageDTO;
import org.example.api.dto.SearchResultDTO;
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
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.Arrays;
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

    @BeforeEach
    void setUp() {
        searchService = new SearchService();
        mockStreamingService = mock(StreamingService.class);
        mockQueryHandlerFactory = mock(SearchQueryHandlerFactory.class);
    }

    @Nested
    @DisplayName("Search Info Tests")
    class SearchInfoTests {

        @Test
        @DisplayName("Should return search results successfully")
        void testGetSearchInfo_Success() throws Exception {
            // Arrange
            String searchString = "java tutorial";
            SearchInfo mockSearchInfo = mock(SearchInfo.class);
            when(mockSearchInfo.getSearchString()).thenReturn(searchString);
            when(mockSearchInfo.getRelatedItems()).thenReturn(Collections.emptyList());
            when(mockSearchInfo.getNextPage()).thenReturn(null);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                when(mockStreamingService.getSearchQHFactory()).thenReturn(mockQueryHandlerFactory);
                searchInfoMock.when(() -> SearchInfo.getInfo(any(), any())).thenReturn(mockSearchInfo);

                // Act
                SearchResultDTO result = searchService.getSearchInfo(searchString, Collections.emptyList(), null);

                // Assert
                assertNotNull(result);
                assertEquals(searchString, result.getSearchString());
                assertNotNull(result.getItems());
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException when NewPipe fails")
        void testGetSearchInfo_ThrowsExtractionException() {
            // Arrange
            String searchString = "test";

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getService(anyInt()))
                        .thenThrow(new RuntimeException("NewPipe failure"));

                // Act & Assert
                ExtractionException exception = assertThrows(ExtractionException.class, () ->
                        searchService.getSearchInfo(searchString, Collections.emptyList(), null)
                );

                assertTrue(exception.getMessage().contains("Failed to retrieve search results"));
            }
        }
    }

    @Nested
    @DisplayName("Search Page Tests")
    class SearchPageTests {

        @Test
        @DisplayName("Should return paginated results successfully")
        void testGetSearchPage_Success() {
            // Arrange
            String searchString = "java";
            String pageUrl = "https://youtube.com/next?page=2";

            ListExtractor.InfoItemsPage mockPage = mock(ListExtractor.InfoItemsPage.class);
            when(mockPage.getItems()).thenReturn(Collections.emptyList());
            when(mockPage.hasNextPage()).thenReturn(false);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                when(mockStreamingService.getSearchQHFactory()).thenReturn(mockQueryHandlerFactory);
                searchInfoMock.when(() -> SearchInfo.getMoreItems(any(), any(), any()))
                        .thenReturn(mockPage);

                // Act
                SearchPageDTO result = searchService.getSearchPage(
                        searchString,
                        Collections.emptyList(),
                        null,
                        pageUrl
                );

                // Assert
                assertNotNull(result);
                assertNotNull(result.getItems());
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException when pagination fails")
        void testGetSearchPage_ThrowsExtractionException() {
            // Arrange
            String searchString = "test";
            String pageUrl = "https://youtube.com/next";

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getService(anyInt()))
                        .thenThrow(new RuntimeException("Pagination failure"));

                // Act & Assert
                ExtractionException exception = assertThrows(ExtractionException.class, () ->
                        searchService.getSearchPage(searchString, Collections.emptyList(), null, pageUrl)
                );

                assertTrue(exception.getMessage().contains("Failed to retrieve search page"));
            }
        }
    }

    @Nested
    @DisplayName("Deduplication Logic Tests")
    class DeduplicationTests {

        @Test
        @DisplayName("Should handle empty search results")
        void testDeduplication_EmptyResults() throws Exception {
            // Arrange
            SearchInfo mockSearchInfo = mock(SearchInfo.class);
            when(mockSearchInfo.getSearchString()).thenReturn("test");
            when(mockSearchInfo.getRelatedItems()).thenReturn(Collections.emptyList());
            when(mockSearchInfo.getNextPage()).thenReturn(null);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<SearchInfo> searchInfoMock = mockStatic(SearchInfo.class)) {

                newPipeMock.when(() -> NewPipe.getService(0)).thenReturn(mockStreamingService);
                when(mockStreamingService.getSearchQHFactory()).thenReturn(mockQueryHandlerFactory);
                searchInfoMock.when(() -> SearchInfo.getInfo(any(), any())).thenReturn(mockSearchInfo);

                // Act
                SearchResultDTO result = searchService.getSearchInfo("test", Collections.emptyList(), null);

                // Assert
                assertNotNull(result.getItems());
                assertTrue(result.getItems().isEmpty());
            }
        }
    }
}