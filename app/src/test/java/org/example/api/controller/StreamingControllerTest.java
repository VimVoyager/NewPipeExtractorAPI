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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StreamingControllerTest {
    private MockMvc mockMvc;
    private static final String BASE_ENDPOINT = "/api/v1/streams/";
    private static final String TEST_URL = "https://www.youtube.com/watch?v=";
    private static final String TEST_VIDEO_ID = "12345";

    @Mock
    private VideoStreamingService videoStreamingService;

    @InjectMocks
    private StreamingController streamingController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(streamingController).build();
    }

    private ResultActions performGetRequest(String endpoint, String id, String expectedResponse) throws Exception {
        String url = TEST_URL + id;

        if (id.isEmpty()) {
            return mockMvc.perform(get(endpoint).param("id", id));
        }

        when(videoStreamingService.getStreamInfo(url)).thenReturn(expectedResponse);

        return mockMvc.perform(get(endpoint).param("id", id));
    }

    @Test
    public void testGetStreamInfo_ValidId_ReturnsOK() throws Exception {
        String expectedResponse = "{\"info\":\"some stream info\"}";

        ResultActions result = performGetRequest(BASE_ENDPOINT, TEST_VIDEO_ID, expectedResponse);

        result.andExpect(status().isOk());
        assertEquals(expectedResponse, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_MissingId_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT));

        result.andExpect(status().isBadRequest());
//        String expectedMessage = "Required request parameter 'id' for method parameter type String is not present";
//        assertEquals(expectedMessage, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        String expectedResponse = "{\"message\":\"Error retrieving stream info\"}";
        ResultActions result = performGetRequest(BASE_ENDPOINT, TEST_VIDEO_ID, expectedResponse);

        result.andExpect(status().isInternalServerError());
        assertEquals(expectedResponse, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_InternalException_ReturnsInternalServerError() throws Exception {
        String expectedResponse = "{\"message\":\"Error retrieving stream info\"}";

        // Mock the service to throw an exception
        when(videoStreamingService.getStreamInfo(BASE_ENDPOINT)).thenThrow(new RuntimeException("Service failure"));
        ResultActions result = performGetRequest(BASE_ENDPOINT, TEST_VIDEO_ID, expectedResponse);

        result.andExpect(status().isInternalServerError());
        assertEquals("{\"message\":\"Error retrieving stream info\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_EmptyId_ReturnsBadRequest() throws Exception {
        String expectedResponse = "{\"message\":\"ID parameter is required\"}";
        ResultActions result = performGetRequest(BASE_ENDPOINT, "", expectedResponse);

        // Assert the results
        result.andExpect(status().isBadRequest());
        assertEquals(expectedResponse, result.andReturn().getResponse().getContentAsString());
    }
}
