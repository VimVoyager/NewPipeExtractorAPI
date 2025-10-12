package org.example.api.controller;

import org.example.api.service.VideoStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
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
    private static final String TEST_VIDEO_ID = "12345";
    private static final String ERROR_MESSAGE = "{\"message\":\"Error retrieving stream info\"}";
    private static final String EMPTY_ID_ERROR_MESSAGE = "{\"message\":\"ID parameter is required\"}";

    @Mock
    private VideoStreamingService videoStreamingService;

    @InjectMocks
    private StreamingController streamingController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(streamingController).build();
    }

    private ResultActions performGetRequest(String videoId) throws Exception {
        String url = TEST_URL + videoId;

        if (videoId.isEmpty()) {
            return mockMvc.perform(get(BASE_ENDPOINT).param("id", videoId));
        }

        when(videoStreamingService.getStreamInfo(url)).thenReturn("{\"info\":\"some stream info\"}");
        return mockMvc.perform(get(BASE_ENDPOINT).param("id", videoId).accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetStreamInfo_ValidId_ReturnsOK() throws Exception {
        ResultActions result = performGetRequest(TEST_VIDEO_ID);

        result.andExpect(status().isOk());
        assertEquals("{\"info\":\"some stream info\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_MissingId_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT)); // No ID provided.
        result.andExpect(status().isBadRequest());
        // assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;
        when(videoStreamingService.getStreamInfo(url)).thenThrow(new RuntimeException("Service failure"));

        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isInternalServerError());
        assertEquals("{\"message\":\"Error retrieving stream info\",\"details\":\"Service failure\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_InternalException_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;
        when(videoStreamingService.getStreamInfo(url)).thenThrow(new RuntimeException("Service failure"));

        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isInternalServerError());
        assertEquals("{\"message\":\"Error retrieving stream info\",\"details\":\"Service failure\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamInfo_EmptyId_ReturnsBadRequest() throws Exception {
        String expectedResponse = "{\"message\":\"ID parameter is required\"}";
        ResultActions result = performGetRequest("");

        // Assert the results
        result.andExpect(status().isBadRequest());
        assertEquals(expectedResponse, result.andReturn().getResponse().getContentAsString());
    }
}
