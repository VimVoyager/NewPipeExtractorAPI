package org.example.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class VideoStreamingServiceTest {
    @Mock
    private ObjectMapper objectMapper;
    private VideoStreamingService videoStreamingService;
    private static final String TEST_URL = "https://example.com/video";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        videoStreamingService = new VideoStreamingService(objectMapper);
    }

    @Test
    public void testGetStreamInfo() throws Exception {
        // Arrange
        StreamInfo mockStreamInfo = mock(StreamInfo.class);
        String expectedJsonResponse = "{\"id\":\"test\"}";

        try (var mockedStatic = mockStatic(StreamInfo.class)) {
            mockedStatic.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

            // Mock JSON serialization
            when(objectMapper.writeValueAsString(mockStreamInfo)).thenReturn(expectedJsonResponse);

            // Act
            String result = videoStreamingService.getStreamInfo(TEST_URL);

            // Assert
            assertEquals(expectedJsonResponse, result);
            verify(objectMapper).writeValueAsString(mockStreamInfo);
        }
    }
}

