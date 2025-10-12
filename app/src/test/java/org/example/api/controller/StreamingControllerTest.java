package org.example.api.controller;

import org.example.api.service.VideoStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StreamingControllerTest {
    private MockMvc mockMvc;
    private static final String BASE_ENDPOINT = "/api/v1/streams/";
    private static final String TEST_URL = "https://www.youtube.com/watch?v=";

    @Mock
    private VideoStreamingService videoStreamingService;

    @InjectMocks
    private StreamingController streamingController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(streamingController).build();
    }

    @Test
    public void testGetStreamInfoSuccess_ValidId_RetunsOK() throws Exception {
        // Arrange
        String id = "12345";
        String expectedResponse = "{\"info\":\"some stream info\"}";
        String url = TEST_URL + id;

        // Mock the service response
        when(videoStreamingService.getStreamInfo(url)).thenReturn(expectedResponse);

        // Act
        // Perform GET request
        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT).param("id", id));

        // Assert
        result.andExpect(status().isOk());
        assertEquals(expectedResponse, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_MissingId_ReturnsBadRequest() throws Exception {
        // Perform GET request with missing id
        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT));

        // Assert the results
        result.andExpect(status().isBadRequest());
//        String expectedMessage = "Required request parameter 'id' for method parameter type String is not present";
//        assertEquals(expectedMessage, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        String id = "12345";
        String url = "https://www.youtube.com/watch?v=" + id;

        // Mock the service to return an error response
        when(videoStreamingService.getStreamInfo(url)).thenReturn("{\"message\":\"error\"}");

        // Perform GET request
        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT).param("id", id));

        // Assert the results
        result.andExpect(status().isInternalServerError());
        assertEquals("{\"message\":\"Error retrieving stream info\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_InternalException_ReturnsInternalServerError() throws Exception {
        String id = "12345";
        String url = "https://www.youtube.com/watch?v=" + id;

        // Mock the service to throw an exception
        when(videoStreamingService.getStreamInfo(url)).thenThrow(new RuntimeException("Service failure"));

        // Perform GET request
        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT).param("id", id));

        // Assert the results
        result.andExpect(status().isInternalServerError());
        assertEquals("{\"message\":\"Error retrieving stream info\",\"details\":\"Service failure\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_EmptyId_ReturnsBadRequest() throws Exception {
        // Perform GET request with empty id
        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT).param("id", ""));

        // Assert the results
        result.andExpect(status().isBadRequest());
        assertEquals("{\"message\":\"ID parameter is required\"}", result.andReturn().getResponse().getContentAsString());
    }
}
