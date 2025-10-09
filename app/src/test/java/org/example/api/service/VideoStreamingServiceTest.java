package org.example.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void testGetAudioStreams() throws Exception {
        // Arrange
        AudioStream mockedAudioStream = mock(AudioStream.class);
        List<AudioStream> mockAudioStreams = Arrays.asList(mockedAudioStream);

        // Assuming that StreamInfo is a class and has a method to get audio streams
        StreamInfo mockedStreamInfo = mock(StreamInfo.class);
        when(mockedStreamInfo.getAudioStreams()).thenReturn(mockAudioStreams);

        String expectedJsonResponse = "[{\"id\":\"test\"}]";

        try (var mockedStatic = mockStatic(StreamInfo.class)) {
            mockedStatic.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockedStreamInfo);

            // Mock JSON serialization
            when(objectMapper.writeValueAsString(mockAudioStreams)).thenReturn(expectedJsonResponse);

            // Act
            String result = videoStreamingService.getAudioStreams(TEST_URL); // Correct method called

            // Assert
            assertEquals(expectedJsonResponse, result);
            verify(objectMapper).writeValueAsString(mockAudioStreams);
        }
    }


}

