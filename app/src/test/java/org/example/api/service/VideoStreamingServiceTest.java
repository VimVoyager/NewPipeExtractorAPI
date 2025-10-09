package org.example.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schabi.newpipe.extractor.stream.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class VideoStreamingServiceTest {
    @Mock
    private ObjectMapper objectMapper;
    private VideoStreamingService videoStreamingService;
    private StreamInfo mockStreamInfo;
    private static final String TEST_URL = "https://example.com/video";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        videoStreamingService = new VideoStreamingService(objectMapper);
        mockStreamInfo = mock(StreamInfo.class);
    }

    @Test
    public void testGetStreamInfo() throws Exception {
        // Arrange
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
        AudioStream mockAudioStream = mock(AudioStream.class);
        List<AudioStream> mockAudioStreams = Arrays.asList(mockAudioStream);

        // Assuming that StreamInfo is a class and has a method to get audio streams
        when(mockStreamInfo.getAudioStreams()).thenReturn(mockAudioStreams);

        String expectedJsonResponse = "[{\"id\":\"test\"}]";

        try (var mockedStatic = mockStatic(StreamInfo.class)) {
            mockedStatic.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

            // Mock JSON serialization
            when(objectMapper.writeValueAsString(mockAudioStreams)).thenReturn(expectedJsonResponse);

            // Act
            String result = videoStreamingService.getAudioStreams(TEST_URL); // Correct method called

            // Assert
            assertEquals(expectedJsonResponse, result);
            verify(objectMapper).writeValueAsString(mockAudioStreams);
        }
    }

    @Test
    public void testGetVideoStreams() throws Exception {
        // Arrange
        VideoStream mockVideoStream = mock(VideoStream.class);
        List<VideoStream> mockVideoStreams = Arrays.asList(mockVideoStream);

        when(mockStreamInfo.getVideoStreams()).thenReturn(mockVideoStreams);

        String expectedJsonResponse = "[{\"id\":\"test\"}]";

        try (var mockedStatic = mockStatic(StreamInfo.class)) {
            mockedStatic.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

            // Mock JSON serialization
            when(objectMapper.writeValueAsString(mockVideoStreams)).thenReturn(expectedJsonResponse);

            // Act
            String result = videoStreamingService.getVideoStreams(TEST_URL);

            // Assert
            assertEquals(expectedJsonResponse, result);
            verify(objectMapper).writeValueAsString(mockVideoStreams);
        }
    }

    @Test
    public void testGetSubtitles() throws Exception {
        // Arrange
        SubtitlesStream mockSubtitles = mock(SubtitlesStream.class);
        List<SubtitlesStream> mockSubtitlesStreams = Arrays.asList(mockSubtitles);

        when(mockStreamInfo.getSubtitles()).thenReturn(mockSubtitlesStreams);

        String expectedJsonResponse = "[{\"id\":\"test\"}]";

        try (var mockedStatic = mockStatic(StreamInfo.class)) {
            mockedStatic.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

            // Mock JSON serialization
            when(objectMapper.writeValueAsString(mockSubtitlesStreams)).thenReturn(expectedJsonResponse);

            // Act
            String result = videoStreamingService.getSubtitleStreams(TEST_URL);

            // Assert
            assertEquals(expectedJsonResponse, result);
            verify(objectMapper).writeValueAsString(mockSubtitlesStreams);
        }
    }

    @Test
    public void testGetStreamSegments() throws Exception {
        // Arrange
        StreamSegment mockStreamSegment = mock(StreamSegment.class);
        List<StreamSegment> mockStreamSegments = Arrays.asList(mockStreamSegment);

        when(mockStreamInfo.getStreamSegments()).thenReturn(mockStreamSegments);

        String expectedJsonResponse = "[{\"id\":\"test\"}]";

        try (var mockedStatic = mockStatic(StreamInfo.class)) {
            mockedStatic.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

            // Mock JSON serialization
            when(objectMapper.writeValueAsString(mockStreamSegments)).thenReturn(expectedJsonResponse);

            // Act
            String result = videoStreamingService.getVideoStreams(TEST_URL);

            // Assert
            assertEquals(expectedJsonResponse, result);
            verify(objectMapper).writeValueAsString(mockStreamSegments);
        }
    }
}


