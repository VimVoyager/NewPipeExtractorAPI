package org.example.api.controller;

import org.example.api.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SearchControllerTest {
    private MockMvc mockMvc;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(searchController).build();
    }

    private ResultActions performGetRequest(int serviceId, String searchString, String sortFilter, String contentFilters) throws Exception {
        return mockMvc.perform(get("/api/v1/search/")
                .param("serviceId", String.valueOf(serviceId))
                .param("searchString", searchString)
                .param("sortFilter", sortFilter)
                .param("contentFilters", contentFilters));
    }

    @Test
    public void testGetSearchInfo_ValidParameters_ReturnsOK() throws Exception {
        // Arrange
        int serviceId = 1;
        String searchString = "example";
        String expectedResponse = "{\"results\":\"some search results\"}";

        when(searchService.getSearchInfo(serviceId, searchString, Collections.emptyList(), null)).thenReturn(expectedResponse);

        // Act
        ResultActions result = performGetRequest(serviceId, searchString, null, null);

        // Assert
        result.andExpect(status().isOk());
        assertEquals(expectedResponse, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetSearchInfo_MissingSearchString_ReturnsBadRequest() throws Exception {
        // Arrange
        int serviceId = 1;
        String searchString = "";

        // Act
        ResultActions result = performGetRequest(serviceId, searchString, null, null);

        // Assert
        result.andExpect(status().isBadRequest());
        assertEquals("{\"message\":\"Search string is required\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetSearchInfo_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        // Arrange
        int serviceId = 1;
        String searchString = "example";

        when(searchService.getSearchInfo(serviceId, searchString, Collections.emptyList(), null)).thenReturn("{\"message\":\"error\"}");

        // Act
        ResultActions result = performGetRequest(serviceId, searchString, null, null);

        // Assert
        result.andExpect(status().isInternalServerError());
        assertEquals("{\"message\":\"Error retrieving search info\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetSearchInfo_InternalException_ReturnsInternalServerError() throws Exception {
        // Arrange
        int serviceId = 1;
        String searchString = "example";

        when(searchService.getSearchInfo(anyInt(), anyString(), anyList(), anyString()))
                .thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = performGetRequest(serviceId, searchString, null, null);

        // Assert
        result.andExpect(status().isInternalServerError());
        assertEquals("{\"message\":\"Error retrieving search info\"}",
                result.andReturn().getResponse().getContentAsString());
    }

}
