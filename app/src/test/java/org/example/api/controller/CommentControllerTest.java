package org.example.api.controller;

import org.example.api.config.GlobalExceptionHandler;
import org.example.api.exception.ExtractionException;
import org.example.api.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerTest {
    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    private static final String TEST_VIDEO_ID = "dQw4w9WgXcQ";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return comments info successfully")
    void testGetComments_Success() throws Exception {
        // Arrange
        CommentsInfo mockCommentsInfo = mock(CommentsInfo.class);
        when(commentService.getCommentsInfo(YOUTUBE_URL + TEST_VIDEO_ID))
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

    @Test
    @DisplayName("Should return 500 when service throws ExtractionException")
    void testGetComments_ServiceException() throws Exception {
        // Arrange
        when(commentService.getCommentsInfo(YOUTUBE_URL + TEST_VIDEO_ID))
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
        when(commentService.getCommentsInfo(YOUTUBE_URL + TEST_VIDEO_ID))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments")
                        .param("id", TEST_VIDEO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
