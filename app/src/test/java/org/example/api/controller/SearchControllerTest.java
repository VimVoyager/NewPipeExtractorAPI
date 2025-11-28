package org.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.config.GlobalExceptionHandler;
import org.example.api.dto.SearchPageDTO;
import org.example.api.dto.SearchResultDTO;
import org.example.api.exception.ExtractionException;
import org.example.api.exception.ValidationException;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
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

        // Setup MockMvc with GlobalExceptionHandler for proper error responses
        mockMvc = MockMvcBuilders.standaloneSetup(searchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

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
                    .andExpect(jsonPath("$.items", hasSize(5)));
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
            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when searchString is empty")
        void testSearch_EmptySearchString() throws Exception {
            // Act & Assert
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
            // Act & Assert
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
            String searchString = "test";
            when(searchService.getSearchInfo(anyString(), anyList(), isNull()))
                    .thenThrow(new ExtractionException("Failed to extract search results"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", searchString)
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
            String searchString = "test";
            SearchResultDTO mockResult = createMockSearchResultDTO(searchString, 1);

            when(searchService.getSearchInfo(eq(searchString), eq(Collections.emptyList()), isNull()))
                    .thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", searchString)
                            .param("contentFilters", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/search/page - Pagination Tests")
    class SearchPageTests {

        @Test
        @DisplayName("Should return paginated results successfully")
        void testSearchPage_Success() throws Exception {
            // Arrange
            String searchString = "java";
            String pageUrl = "https://youtube.com/next?page=2";
            SearchPageDTO mockPage = createMockSearchPageDTO(3);

            when(searchService.getSearchPage(
                    eq(searchString),
                    anyList(),
                    isNull(),
                    eq(pageUrl)
            )).thenReturn(mockPage);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", searchString)
                            .param("pageUrl", pageUrl)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items", hasSize(3)))
                    .andExpect(jsonPath("$.itemCount").value(3));
        }

        @Test
        @DisplayName("Should return 400 when searchString is missing")
        void testSearchPage_MissingSearchString() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("pageUrl", "https://youtube.com/next")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when pageUrl is missing")
        void testSearchPage_MissingPageUrl() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "test")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when both parameters are empty")
        void testSearchPage_EmptyParameters() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "")
                            .param("pageUrl", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle pagination with filters")
        void testSearchPage_WithFilters() throws Exception {
            // Arrange
            String searchString = "python";
            String pageUrl = "https://youtube.com/next";
            String sortFilter = "relevance";
            String contentFilters = "video";
            SearchPageDTO mockPage = createMockSearchPageDTO(2);

            when(searchService.getSearchPage(
                    eq(searchString),
                    eq(Collections.singletonList("video")),
                    eq(sortFilter),
                    eq(pageUrl)
            )).thenReturn(mockPage);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", searchString)
                            .param("pageUrl", pageUrl)
                            .param("sortFilter", sortFilter)
                            .param("contentFilters", contentFilters)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.itemCount").value(2));
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void testSearchPage_ServiceThrowsException() throws Exception {
            // Arrange
            when(searchService.getSearchPage(anyString(), anyList(), isNull(), anyString()))
                    .thenThrow(new ExtractionException("Failed to retrieve page"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/search/page")
                            .param("searchString", "test")
                            .param("pageUrl", "https://youtube.com/next")
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
                    anyString(),
                    eq(Collections.singletonList("video")),
                    isNull()
            )).thenReturn(mockResult);

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
                    anyString(),
                    eq(Arrays.asList("video", "channel", "playlist")),
                    isNull()
            )).thenReturn(mockResult);

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
            when(searchService.getSearchInfo(
                    anyString(),
                    eq(Collections.emptyList()),
                    isNull()
            )).thenReturn(mockResult);

            // Act & Assert
            mockMvc.perform(get("/api/v1/search")
                            .param("searchString", "test")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // Helper methods
    private SearchResultDTO createMockSearchResultDTO(String searchString, int itemCount) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setSearchString(searchString);
        dto.setItems(Collections.nCopies(itemCount, null)); // Simplified for testing
        dto.setHasNextPage(true);
        dto.setNextPageUrl("https://youtube.com/next");
        return dto;
    }

    private SearchPageDTO createMockSearchPageDTO(int itemCount) {
        SearchPageDTO dto = new SearchPageDTO();
        dto.setItems(Collections.nCopies(itemCount, null)); // Simplified for testing
        dto.setItemCount(itemCount);
        dto.setHasNextPage(false);
        return dto;
    }
}