package org.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.config.GlobalExceptionHandler;
import org.example.api.dto.search.SearchPageDTO;
import org.example.api.dto.search.SearchResultDTO;
import org.example.api.exception.ExtractionException;
import org.example.api.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for SearchController.
 * Tests all endpoints with proper validation and error handling.
 */
@DisplayName("SearchController Tests")
class SearchControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(searchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private SearchResultDTO createMockSearchResultDTO(String searchString, int itemCount) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setSearchString(searchString);
        dto.setItems(Collections.nCopies(itemCount, null));
        dto.setHasNextPage(true);
        dto.setNextPage(new SearchResultDTO.PageDto(
                "https://www.youtube.com/youtubei/v1/search?prettyPrint=false",
                "eyJjb250aW51YXRpb24iOiJ0b2tlbiJ9"
        ));
        return dto;
    }

    private SearchPageDTO createMockSearchPageDTO(int itemCount) {
        SearchPageDTO dto = new SearchPageDTO();
        dto.setItems(Collections.nCopies(itemCount, null));
        dto.setItemCount(itemCount);
        dto.setHasNextPage(false);
        dto.setNextPage(null);
        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search - Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should return search results successfully")
        void testSearch_Success() throws Exception {
            // Arrange
            String searchString = "java tutorial";
            SearchResultDTO mockResult = createMockSearchResultDTO(searchString, 5);

            when(searchService.getSearchInfo(eq(searchString), anyList(), isNull()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", searchString)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.searchString").value(searchString))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", hasSize(5)))
                    .andExpect(jsonPath("$.nextPage.url").isNotEmpty())
                    .andExpect(jsonPath("$.nextPage.id").isNotEmpty());
        }

        @Test
        @DisplayName("Should return results with filters and sort")
        void testSearch_WithFiltersAndSort() throws Exception {
            // Arrange
            String searchString = "programming";
            String sortFilter = "upload_date";
            String contentFilters = "video,channel";
            SearchResultDTO mockResult = createMockSearchResultDTO(searchString, 3);

            when(searchService.getSearchInfo(
                    eq(searchString),
                    eq(Arrays.asList("video", "channel")),
                    eq(sortFilter)
            )).thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", searchString)
                            .param("sortFilter", sortFilter)
                            .param("contentFilters", contentFilters)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.searchString").value(searchString));
        }

        @Test
        @DisplayName("Should return 400 when searchString is missing")
        void testSearch_MissingSearchString() throws Exception {
            mockMvc.perform(get("/api/v1/search")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when searchString is empty")
        void testSearch_EmptySearchString() throws Exception {
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").value(containsString("searchString")));
        }

        @Test
        @DisplayName("Should return 400 when searchString is only whitespace")
        void testSearch_WhitespaceSearchString() throws Exception {
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", "   ")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("Should return 500 when service throws ExtractionException")
        void testSearch_ServiceThrowsException() throws Exception {
            // Arrange
            when(searchService.getSearchInfo(anyString(), anyList(), isNull()))
                    .thenThrow(new ExtractionException("Failed to extract search results"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", "test")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.errorCode").value("EXTRACTION_ERROR"))
                    .andExpect(jsonPath("$.message").value(containsString("Failed to extract")));
        }

        @Test
        @DisplayName("Should handle empty content filters")
        void testSearch_EmptyContentFilters() throws Exception {
            // Arrange
            SearchResultDTO mockResult = createMockSearchResultDTO("test", 1);
            when(searchService.getSearchInfo(eq("test"), eq(Collections.emptyList()), isNull()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", "test")
                            .param("contentFilters", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/search/page - Pagination Tests")
    class SearchPageTests {

        private static final String PAGE_URL = "https://www.youtube.com/youtubei/v1/search?prettyPrint=false";
        private static final String PAGE_ID = "eyJjb250aW51YXRpb24iOiJ0b2tlbiJ9";

        @Test
        @DisplayName("Should return paginated results successfully")
        void testSearchPage_Success() throws Exception {
            // Arrange
            String searchString = "java";
            SearchPageDTO mockPage = createMockSearchPageDTO(3);

            when(searchService.getSearchPage(
                    eq(searchString), anyList(), isNull(), eq(PAGE_URL), eq(PAGE_ID)))
                    .thenReturn(mockPage);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", searchString)
                            .param("pageUrl", PAGE_URL)
                            .param("pageId", PAGE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", hasSize(3)))
                    .andExpect(jsonPath("$.itemCount").value(3));
        }

        @Test
        @DisplayName("Should pass pageUrl and pageId verbatim to service")
        void testSearchPage_PassesParamsToService() throws Exception {
            // Arrange
            SearchPageDTO mockPage = createMockSearchPageDTO(1);
            when(searchService.getSearchPage(anyString(), anyList(), isNull(), anyString(), anyString()))
                    .thenReturn(mockPage);

            // Act
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "linux")
                            .param("pageUrl", PAGE_URL)
                            .param("pageId", PAGE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Assert
            verify(searchService).getSearchPage(
                    eq("linux"), anyList(), isNull(), eq(PAGE_URL), eq(PAGE_ID));
        }

        @Test
        @DisplayName("Should return 400 when searchString is missing")
        void testSearchPage_MissingSearchString() throws Exception {
            mockMvc.perform(get("/api/v1/search/page")
                            .param("pageUrl", PAGE_URL)
                            .param("pageId", PAGE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when pageUrl is missing")
        void testSearchPage_MissingPageUrl() throws Exception {
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "test")
                            .param("pageId", PAGE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 200 when pageId is absent (null body pages are valid)")
        void testSearchPage_MissingPageBody() throws Exception {
            // Arrange — pageId is optional; some next pages are URL-only continuations
            SearchPageDTO mockPage = createMockSearchPageDTO(2);
            when(searchService.getSearchPage(
                    eq("test"), anyList(), isNull(), eq(PAGE_URL), isNull()))
                    .thenReturn(mockPage);

            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "test")
                            .param("pageUrl", PAGE_URL)
                            // no pageId param
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should pass null pageId to service when param is absent")
        void testSearchPage_NullPageBodyPassedToService() throws Exception {
            // Arrange
            SearchPageDTO mockPage = createMockSearchPageDTO(1);
            when(searchService.getSearchPage(anyString(), anyList(), isNull(), anyString(), isNull()))
                    .thenReturn(mockPage);

            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "test")
                            .param("pageUrl", PAGE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(searchService).getSearchPage(
                    eq("test"), anyList(), isNull(), eq(PAGE_URL), isNull());
        }

        @Test
        @DisplayName("Should handle pagination with filters")
        void testSearchPage_WithFilters() throws Exception {
            // Arrange
            SearchPageDTO mockPage = createMockSearchPageDTO(2);
            when(searchService.getSearchPage(
                    eq("python"),
                    eq(Collections.singletonList("video")),
                    eq("relevance"),
                    eq(PAGE_URL),
                    eq(PAGE_ID)
            )).thenReturn(mockPage);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "python")
                            .param("pageUrl", PAGE_URL)
                            .param("pageId", PAGE_ID)
                            .param("sortFilter", "relevance")
                            .param("contentFilters", "video")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemCount").value(2));
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void testSearchPage_ServiceThrowsException() throws Exception {
            // Arrange
            when(searchService.getSearchPage(anyString(), anyList(), isNull(), anyString(), anyString()))
                    .thenThrow(new ExtractionException("Failed to retrieve page"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "test")
                            .param("pageUrl", PAGE_URL)
                            .param("pageId", PAGE_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode").value("EXTRACTION_ERROR"));
        }
    }

    @Nested
    @DisplayName("Content Filter Parsing Tests")
    class ContentFilterParsingTests {

        @Test
        @DisplayName("Should parse single content filter")
        void testParseContentFilters_Single() throws Exception {
            // Arrange
            SearchResultDTO mockResult = createMockSearchResultDTO("test", 1);
            when(searchService.getSearchInfo(
                    anyString(), eq(Collections.singletonList("video")), isNull()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", "test")
                            .param("contentFilters", "video")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should parse multiple content filters")
        void testParseContentFilters_Multiple() throws Exception {
            // Arrange
            SearchResultDTO mockResult = createMockSearchResultDTO("test", 1);
            when(searchService.getSearchInfo(
                    anyString(), eq(Arrays.asList("video", "channel", "playlist")), isNull()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", "test")
                            .param("contentFilters", "video,channel,playlist")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle null content filters")
        void testParseContentFilters_Null() throws Exception {
            // Arrange
            SearchResultDTO mockResult = createMockSearchResultDTO("test", 1);
            when(searchService.getSearchInfo(anyString(), eq(Collections.emptyList()), isNull()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", "test")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}