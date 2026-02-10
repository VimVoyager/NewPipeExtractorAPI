package org.example.api.controller;

import org.example.api.config.GlobalExceptionHandler;
import org.example.api.exception.ExtractionException;
import org.example.api.service.RestService;
import org.example.api.service.SearchService;
import org.example.api.service.VideoStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NewPipeControllerTest {
    private MockMvc mockMvc;
    @Mock
    private RestService restService;
    private VideoStreamingService videoStreamingService;
    private SearchService searchService;

    @InjectMocks
    private NewPipeController newPipeController;

    private static final String TEST_VIDEO_ID = "dQw4w9WgXcQ";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(newPipeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testGetServiceSuccess() throws Exception {
        // Arrange
        String expectedJson = "{\"serviceName\":\"Example Service\"}";
        when(restService.getServices()).thenReturn(expectedJson);

        // Act
        ResponseEntity<?> response = newPipeController.getServices();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    void testGetServicesErrorResponse() throws Exception {
        // Arrange
        when(restService.getServices()).thenReturn("{\"message\":\"Some error occurred\"}");

        // Act
        ResponseEntity<?> response = newPipeController.getServices();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        Map<String, String> bodyMap = (Map<String, String>) response.getBody();
        assertEquals("Error retrieving services", bodyMap.get("message"));
    }

    @Test
    void testGetServicesException() throws Exception {
        // Arrange
        when(restService.getServices()).thenThrow(new RuntimeException("Service failure"));

        // Act
        ResponseEntity<?> response = newPipeController.getServices();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());
        Map<String, String> bodyMap = (Map<String, String>) response.getBody();
        assertEquals("Error retrieving services: Service failure", bodyMap.get("message"));
    }

    @Test
    @DisplayName("Should return comments info successfully")
    void testGetComments_Success() throws Exception {
        // Arrange
        CommentsInfo mockCommentsInfo = mock(CommentsInfo.class);
        when(restService.getCommentsInfo(YOUTUBE_URL + TEST_VIDEO_ID))
                .thenReturn(mockCommentsInfo);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments")
                        .param("id", TEST_VIDEO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 400 when id parameter is missing")
    void testGetComments_MissingId() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

//    @Test
//    @DisplayName("Should return 400 when URL validation fails")
//    void testGetComments_InvalidUrl() throws Exception {
//        // Arrange
//        String invalidId = "../../malicious";
//
//        // Act & Assert
//        mockMvc.perform(get("/api/v1/comments")
//                        .param("id", invalidId)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    @DisplayName("Should return 500 when service throws ExtractionException")
    void testGetComments_ServiceException() throws Exception {
        // Arrange
        when(restService.getCommentsInfo(YOUTUBE_URL + TEST_VIDEO_ID))
                .thenThrow(new ExtractionException("Failed to retrieve comments information"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments")
                        .param("id", TEST_VIDEO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return 500 when service throws generic exception")
    void testGetComments_GenericException() throws Exception {
        // Arrange
        when(restService.getCommentsInfo(YOUTUBE_URL + TEST_VIDEO_ID))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments")
                        .param("id", TEST_VIDEO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
