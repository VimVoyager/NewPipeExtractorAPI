package org.example.api.service;

import org.example.api.exception.ExtractionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for VideoStreamingService.
 * Tests all stream extraction methods and error handling.
 */
@DisplayName("VideoStreamingService Tests")
class VideoStreamingServiceTest {

    private VideoStreamingService videoStreamingService;
    private static final String TEST_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    @BeforeEach
    void setUp() {
        videoStreamingService = new VideoStreamingService();
    }

    @Nested
    @DisplayName("Stream Info Tests")
    class StreamInfoTests {

        @Test
        @DisplayName("Should return stream info successfully")
        void testGetStreamInfo_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                StreamInfo result = videoStreamingService.getStreamInfo(TEST_URL);

                // Assert
                assertNotNull(result);
                assertEquals("Test Video", result.getName());
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException when extraction fails")
        void testGetStreamInfo_ThrowsExtractionException() {
            // Arrange
            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL))
                        .thenThrow(new RuntimeException("Extraction failed"));

                // Act & Assert
                ExtractionException exception = assertThrows(ExtractionException.class, () ->
                        videoStreamingService.getStreamInfo(TEST_URL)
                );

                assertTrue(exception.getMessage().contains("Failed to retrieve stream information"));
                assertNotNull(exception.getCause());
            }
        }
    }

    @Nested
    @DisplayName("Audio Stream Tests")
    class AudioStreamTests {

        @Test
        @DisplayName("Should return audio streams successfully")
        void testGetAudioStreams_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            List<AudioStream> mockAudioStreams = createMockAudioStreams();
            when(mockStreamInfo.getAudioStreams()).thenReturn(mockAudioStreams);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                List<AudioStream> result = videoStreamingService.getAudioStreams(TEST_URL);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());
            }
        }

        @Test
        @DisplayName("Should return empty list when no audio streams available")
        void testGetAudioStreams_EmptyList() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            when(mockStreamInfo.getAudioStreams()).thenReturn(Collections.emptyList());

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                List<AudioStream> result = videoStreamingService.getAudioStreams(TEST_URL);

                // Assert
                assertNotNull(result);
                assertTrue(result.isEmpty());
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException when extraction fails")
        void testGetAudioStreams_ThrowsExtractionException() {
            // Arrange
            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL))
                        .thenThrow(new RuntimeException("Failed"));

                // Act & Assert
                ExtractionException exception = assertThrows(ExtractionException.class, () ->
                        videoStreamingService.getAudioStreams(TEST_URL)
                );

                assertTrue(exception.getMessage().contains("Failed to retrieve audio streams"));
            }
        }
    }

    @Nested
    @DisplayName("Video Stream Tests")
    class VideoStreamTests {

        @Test
        @DisplayName("Should return video streams successfully")
        void testGetVideoStreams_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            List<VideoStream> mockVideoStreams = createMockVideoStreams();
            when(mockStreamInfo.getVideoOnlyStreams()).thenReturn(mockVideoStreams);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                List<VideoStream> result = videoStreamingService.getVideoStreams(TEST_URL);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException when extraction fails")
        void testGetVideoStreams_ThrowsExtractionException() {
            // Arrange
            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL))
                        .thenThrow(new RuntimeException("Failed"));

                // Act & Assert
                ExtractionException exception = assertThrows(ExtractionException.class, () ->
                        videoStreamingService.getVideoStreams(TEST_URL)
                );

                assertTrue(exception.getMessage().contains("Failed to retrieve video streams"));
            }
        }
    }

    @Nested
    @DisplayName("DASH MPD Tests")
    class DashMpdTests {

        @Test
        @DisplayName("Should return DASH MPD URL successfully")
        void testGetDashMpdUrl_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            String dashUrl = "https://youtube.com/dash/manifest.mpd";
            when(mockStreamInfo.getDashMpdUrl()).thenReturn(dashUrl);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                String result = videoStreamingService.getDashMpdUrl(TEST_URL);

                // Assert
                assertEquals(dashUrl, result);
            }
        }

        @Test
        @DisplayName("Should handle null DASH MPD URL")
        void testGetDashMpdUrl_Null() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            when(mockStreamInfo.getDashMpdUrl()).thenReturn(null);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                String result = videoStreamingService.getDashMpdUrl(TEST_URL);

                // Assert
                assertNull(result);
            }
        }
    }

    @Nested
    @DisplayName("Subtitle Tests")
    class SubtitleTests {

        @Test
        @DisplayName("Should return subtitle streams successfully")
        void testGetSubtitleStreams_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            List<SubtitlesStream> mockSubtitles = createMockSubtitles();
            when(mockStreamInfo.getSubtitles()).thenReturn(mockSubtitles);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                List<SubtitlesStream> result = videoStreamingService.getSubtitleStreams(TEST_URL);

                // Assert
                assertNotNull(result);
                assertEquals(2, result.size());
            }
        }
    }

    @Nested
    @DisplayName("Stream Segment Tests")
    class StreamSegmentTests {

        @Test
        @DisplayName("Should return stream segments successfully")
        void testGetStreamSegments_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            List<StreamSegment> mockSegments = createMockSegments();
            when(mockStreamInfo.getStreamSegments()).thenReturn(mockSegments);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                List<StreamSegment> result = videoStreamingService.getStreamSegments(TEST_URL);

                // Assert
                assertNotNull(result);
                assertEquals(3, result.size());
            }
        }
    }

    @Nested
    @DisplayName("Preview Frame Tests")
    class PreviewFrameTests {

        @Test
        @DisplayName("Should return preview frames successfully")
        void testGetPreviewFrames_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            List<Frameset> mockFrames = createMockFramesets();
            when(mockStreamInfo.getPreviewFrames()).thenReturn(mockFrames);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                List<Frameset> result = videoStreamingService.getPreviewFrames(TEST_URL);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.size());
            }
        }
    }

    @Nested
    @DisplayName("Description Tests")
    class DescriptionTests {

        @Test
        @DisplayName("Should return description successfully")
        void testGetStreamDescription_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            Description mockDescription = createMockDescription();
            when(mockStreamInfo.getDescription()).thenReturn(mockDescription);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                Description result = videoStreamingService.getStreamDescription(TEST_URL);

                // Assert
                assertNotNull(result);
            }
        }
    }

    @Nested
    @DisplayName("Related Streams Tests")
    class RelatedStreamsTests {

        @Test
        @DisplayName("Should return related streams successfully")
        void testGetRelatedStreams_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            List<InfoItem> mockRelated = createMockRelatedItems();
            when(mockStreamInfo.getRelatedItems()).thenReturn(mockRelated);

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                List<InfoItem> result = videoStreamingService.getRelatedStreams(TEST_URL);

                // Assert
                assertNotNull(result);
                assertEquals(5, result.size());
            }
        }

        @Test
        @DisplayName("Should return empty list when no related streams")
        void testGetRelatedStreams_EmptyList() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = createMockStreamInfo();
            when(mockStreamInfo.getRelatedItems()).thenReturn(Collections.emptyList());

            try (MockedStatic<StreamInfo> streamInfoMock = mockStatic(StreamInfo.class)) {
                streamInfoMock.when(() -> StreamInfo.getInfo(TEST_URL)).thenReturn(mockStreamInfo);

                // Act
                List<InfoItem> result = videoStreamingService.getRelatedStreams(TEST_URL);

                // Assert
                assertNotNull(result);
                assertTrue(result.isEmpty());
            }
        }
    }

    // Helper methods to create mock objects
    private StreamInfo createMockStreamInfo() {
        StreamInfo mockInfo = mock(StreamInfo.class);
        when(mockInfo.getName()).thenReturn("Test Video");
        when(mockInfo.getUrl()).thenReturn(TEST_URL);
        return mockInfo;
    }

    private List<AudioStream> createMockAudioStreams() {
        AudioStream stream1 = mock(AudioStream.class);
        AudioStream stream2 = mock(AudioStream.class);
        return Arrays.asList(stream1, stream2);
    }

    private List<VideoStream> createMockVideoStreams() {
        VideoStream stream1 = mock(VideoStream.class);
        VideoStream stream2 = mock(VideoStream.class);
        return Arrays.asList(stream1, stream2);
    }

    private List<SubtitlesStream> createMockSubtitles() {
        SubtitlesStream sub1 = mock(SubtitlesStream.class);
        SubtitlesStream sub2 = mock(SubtitlesStream.class);
        return Arrays.asList(sub1, sub2);
    }

    private List<StreamSegment> createMockSegments() {
        StreamSegment seg1 = mock(StreamSegment.class);
        StreamSegment seg2 = mock(StreamSegment.class);
        StreamSegment seg3 = mock(StreamSegment.class);
        return Arrays.asList(seg1, seg2, seg3);
    }

    private List<Frameset> createMockFramesets() {
        Frameset frameset = mock(Frameset.class);
        return Collections.singletonList(frameset);
    }

    private Description createMockDescription() {
        return mock(Description.class);
    }

    private List<InfoItem> createMockRelatedItems() {
        return Arrays.asList(
                mock(InfoItem.class),
                mock(InfoItem.class),
                mock(InfoItem.class),
                mock(InfoItem.class),
                mock(InfoItem.class)
        );
    }
}
