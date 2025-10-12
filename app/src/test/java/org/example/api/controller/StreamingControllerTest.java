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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StreamingControllerTest {
    private MockMvc mockMvc;

    private static final String BASE_ENDPOINT = "/api/v1/streams/";
    private static final String AUDIO_ENDPOINT = BASE_ENDPOINT + "audio";
    private static final String VIDEO_ENDPOINT = BASE_ENDPOINT + "video";
    private static final String SEGMENTS_ENDPOINT = BASE_ENDPOINT + "segments";
    private static final String FRAMES_ENDPOINT = BASE_ENDPOINT + "frames";
    private static final String DESCRIPTION_ENDPOINT = BASE_ENDPOINT + "description";
    private static final String TEST_URL = "https://www.youtube.com/watch?v=";
    private static final String TEST_VIDEO_ID = "12345";
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

    private ResultActions performGetAudioRequest(String videoId) throws Exception {
        String url = TEST_URL + videoId;

        if (videoId.isEmpty()) {
            return mockMvc.perform(get(AUDIO_ENDPOINT).param("id", videoId));
        }

        when(videoStreamingService.getAudioStreams(url)).thenReturn("{\"audio\":\"some audio stream info\"}");
        return mockMvc.perform(get(AUDIO_ENDPOINT).param("id", videoId).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGetVideoRequest(String videoId) throws Exception {
        String url = TEST_URL + videoId;

        if (videoId.isEmpty()) {
            return mockMvc.perform(get(VIDEO_ENDPOINT).param("id", videoId));
        }

        when(videoStreamingService.getVideoStreams(url)).thenReturn("{\"video\":\"some video stream info\"}");
        return mockMvc.perform(get(VIDEO_ENDPOINT).param("id", videoId).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGetSegmentRequest(String videoId) throws Exception {
        String url = TEST_URL + videoId;

        if (videoId.isEmpty()) {
            return mockMvc.perform(get(SEGMENTS_ENDPOINT).param("id", videoId));
        }

        when(videoStreamingService.getStreamSegments(url)).thenReturn("{\"segments\":\"some stream segments info\"}");
        return mockMvc.perform(get(SEGMENTS_ENDPOINT).param("id", videoId).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGetFrameRequest(String videoId) throws Exception {
        String url = TEST_URL + videoId;

        if (videoId.isEmpty()) {
            return mockMvc.perform(get(FRAMES_ENDPOINT).param("id", videoId));
        }

        when(videoStreamingService.getPreviewFrames(url)).thenReturn("{\"frames\":\"some preview frames info\"}");
        return mockMvc.perform(get(FRAMES_ENDPOINT).param("id", videoId).accept(MediaType.APPLICATION_JSON));
    }

    private ResultActions performGetDescriptionRequest(String videoId) throws Exception {
        String url = TEST_URL + videoId;

        if (videoId.isEmpty()) {
            return mockMvc.perform(get(DESCRIPTION_ENDPOINT).param("id", videoId));
        }

        when(videoStreamingService.getStreamDescription(url)).thenReturn("{\"description\":\"some stream description\"}");
        return mockMvc.perform(get(DESCRIPTION_ENDPOINT).param("id", videoId).accept(MediaType.APPLICATION_JSON));
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
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving stream info"));
    }

    @Test
    public void testGetStreamInfo_InternalException_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;
        when(videoStreamingService.getStreamInfo(url)).thenThrow(new RuntimeException("Service failure"));

        ResultActions result = mockMvc.perform(get(BASE_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving stream info"));
    }

    @Test
    public void testGetStreamInfo_EmptyId_ReturnsBadRequest() throws Exception {
        ResultActions result = performGetRequest("");

        // Assert the results
        result.andExpect(status().isBadRequest());
        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetAudioStreams_ValidId_ReturnsOK() throws Exception {
        ResultActions result = performGetAudioRequest(TEST_VIDEO_ID);

        result.andExpect(status().isOk());
        assertEquals("{\"audio\":\"some audio stream info\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetAudioStreams_MissingId_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(get(AUDIO_ENDPOINT)); // No ID provided.
        result.andExpect(status().isBadRequest());
//        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetAudioStreams_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;
        when(videoStreamingService.getAudioStreams(url)).thenThrow(new RuntimeException("Service failure"));

        ResultActions result = mockMvc.perform(get(AUDIO_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving audio stream"));
    }

    @Test
    public void testGetAudioStreams_InternalException_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;
        when(videoStreamingService.getAudioStreams(url)).thenThrow(new RuntimeException("Service failure"));

        ResultActions result = mockMvc.perform(get(AUDIO_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving audio stream"));
    }

    @Test
    public void testGetAudioStreams_EmptyId_ReturnsBadRequest() throws Exception {
        ResultActions result = performGetAudioRequest("");

        // Assert the results
        result.andExpect(status().isBadRequest());
        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetVideoStreams_ValidId_ReturnsOK() throws Exception {
        // Act
        ResultActions result = performGetVideoRequest(TEST_VIDEO_ID);

        // Assert
        result.andExpect(status().isOk());
        assertEquals("{\"video\":\"some video stream info\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetVideoStreams_MissingId_ReturnsBadRequest() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get(VIDEO_ENDPOINT)); // No ID provided.

        // Assert
        result.andExpect(status().isBadRequest());
//        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetVideoStreams_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;

        // Simulate an error return from the service
        when(videoStreamingService.getVideoStreams(url)).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = mockMvc.perform(get(VIDEO_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving video stream"));
    }

    @Test
    public void testGetVideoStreams_InternalException_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;

        // Simulate an internal exception
        when(videoStreamingService.getVideoStreams(url)).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = mockMvc.perform(get(VIDEO_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving video stream"));
    }

    @Test
    public void testGetVideoStreams_EmptyId_ReturnsBadRequest() throws Exception {
        // Act
        ResultActions result = performGetVideoRequest("");

        // Assert
        result.andExpect(status().isBadRequest());
        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamSegments_ValidId_ReturnsOK() throws Exception {
        // Act
        ResultActions result = performGetSegmentRequest(TEST_VIDEO_ID);

        // Assert
        result.andExpect(status().isOk());
        assertEquals("{\"segments\":\"some stream segments info\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamSegments_MissingId_ReturnsBadRequest() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get(SEGMENTS_ENDPOINT)); // No ID provided.

        // Assert
        result.andExpect(status().isBadRequest());
//        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamSegments_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;

        // Simulating an error return from the service
        when(videoStreamingService.getStreamSegments(url)).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = mockMvc.perform(get(SEGMENTS_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving stream segments"));
    }

    @Test
    public void testGetStreamSegments_InternalException_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;

        // Simulate an internal exception
        when(videoStreamingService.getStreamSegments(url)).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = mockMvc.perform(get(SEGMENTS_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving stream segments"));
    }

    @Test
    public void testGetStreamSegments_EmptyId_ReturnsBadRequest() throws Exception {
        // Act
        ResultActions result = performGetSegmentRequest("");

        // Assert
        result.andExpect(status().isBadRequest());
        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetPreviewFrames_ValidId_ReturnsOK() throws Exception {
        // Act
        ResultActions result = performGetFrameRequest(TEST_VIDEO_ID);

        // Assert
        result.andExpect(status().isOk());
        assertEquals("{\"frames\":\"some preview frames info\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetPreviewFrames_MissingId_ReturnsBadRequest() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get(FRAMES_ENDPOINT)); // No ID provided.

        // Assert
        result.andExpect(status().isBadRequest());
//        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetPreviewFrames_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;

        // Simulate an error return from the service
        when(videoStreamingService.getPreviewFrames(url)).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = mockMvc.perform(get(FRAMES_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving preview frames"));
    }

    @Test
    public void testGetPreviewFrames_InternalException_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;

        // Simulate an internal exception
        when(videoStreamingService.getPreviewFrames(url)).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = mockMvc.perform(get(FRAMES_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving preview frames"));
    }

    @Test
    public void testGetPreviewFrames_EmptyId_ReturnsBadRequest() throws Exception {
        // Act
        ResultActions result = performGetFrameRequest("");

        // Assert
        result.andExpect(status().isBadRequest());
        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamDescription_ValidId_ReturnsOK() throws Exception {
        // Act
        ResultActions result = performGetDescriptionRequest(TEST_VIDEO_ID);

        // Assert
        result.andExpect(status().isOk());
        assertEquals("{\"description\":\"some stream description\"}", result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamDescription_MissingId_ReturnsBadRequest() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get(DESCRIPTION_ENDPOINT)); // No ID provided.

        // Assert
        result.andExpect(status().isBadRequest());
//        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testGetStreamDescription_ErrorResponseFromService_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;

        // Simulate an error return from the service
        when(videoStreamingService.getStreamDescription(url)).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = mockMvc.perform(get(DESCRIPTION_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving description"));
    }

    @Test
    public void testGetStreamDescription_InternalException_ReturnsInternalServerError() throws Exception {
        String url = TEST_URL + TEST_VIDEO_ID;

        // Simulate an internal exception
        when(videoStreamingService.getStreamDescription(url)).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResultActions result = mockMvc.perform(get(DESCRIPTION_ENDPOINT).param("id", TEST_VIDEO_ID).accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isInternalServerError());
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Service failure"));
        assertTrue(result.andReturn().getResponse().getContentAsString().contains("Error retrieving description"));
    }

    @Test
    public void testGetStreamDescription_EmptyId_ReturnsBadRequest() throws Exception {
        // Act
        ResultActions result = performGetDescriptionRequest("");

        // Assert
        result.andExpect(status().isBadRequest());
        assertEquals(EMPTY_ID_ERROR_MESSAGE, result.andReturn().getResponse().getContentAsString());
    }
}
