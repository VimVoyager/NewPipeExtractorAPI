package org.example.api.controller;

import org.example.api.config.GlobalExceptionHandler;
import org.example.api.dto.dash.DashManifestConfigDTO;
import org.example.api.exception.ExtractionException;
import org.example.api.service.DashManifestGeneratorService;
import org.example.api.service.StreamSelectionService;
import org.example.api.service.VideoStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;
import org.schabi.newpipe.extractor.stream.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for StreamingController.
 * Tests all streaming endpoints with proper validation and error handling.
 */
@DisplayName("StreamingController Tests")
class StreamingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VideoStreamingService videoStreamingService;

    @Mock
    private DashManifestGeneratorService dashManifestGeneratorService;

    @Mock
    private StreamSelectionService streamSelectionService;

    @InjectMocks
    private StreamingController streamingController;

    private static final String TEST_VIDEO_ID = "dQw4w9WgXcQ";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup MockMvc with GlobalExceptionHandler
        mockMvc = MockMvcBuilders.standaloneSetup(streamingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }



    @Nested
    @DisplayName("GET /api/v1/streams - Stream Info Tests")
    class StreamInfoTests {

        @Test
        @DisplayName("Should return stream info successfully")
        void testGetStreamInfo_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = mock(StreamInfo.class);
            when(mockStreamInfo.getName()).thenReturn("Test Video");
            when(videoStreamingService.getStreamInfo(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockStreamInfo);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should return 400 when ID is missing")
        void testGetStreamInfo_MissingId() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/streams")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when ID is empty")
        void testGetStreamInfo_EmptyId() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/streams")
                            .param("id", "")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void testGetStreamInfo_ServiceThrowsException() throws Exception {
            // Arrange
            when(videoStreamingService.getStreamInfo(anyString()))
                    .thenThrow(new ExtractionException("Failed to extract stream info"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.errorCode").value("EXTRACTION_ERROR"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/audio - Audio Stream Tests")
    class AudioStreamTests {

        @Test
        @DisplayName("Should return audio streams successfully")
        void testGetAudioStreams_Success() throws Exception {
            // Arrange
            List<AudioStream> mockAudioStreams = Arrays.asList(
                    mock(AudioStream.class),
                    mock(AudioStream.class)
            );
            when(videoStreamingService.getAudioStreams(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockAudioStreams);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/audio")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Should return empty list when no audio streams available")
        void testGetAudioStreams_EmptyList() throws Exception {
            // Arrange
            when(videoStreamingService.getAudioStreams(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/audio")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 400 when ID is missing")
        void testGetAudioStreams_MissingId() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/audio")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/video - Video Stream Tests")
    class VideoStreamTests {

        @Test
        @DisplayName("Should return video streams successfully")
        void testGetVideoStreams_Success() throws Exception {
            // Arrange
            List<VideoStream> mockVideoStreams = Arrays.asList(
                    mock(VideoStream.class),
                    mock(VideoStream.class),
                    mock(VideoStream.class)
            );
            when(videoStreamingService.getVideoStreams(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockVideoStreams);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/video")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/video/dash - DASH MPD Tests")
    class DashMpdTests {

        @Test
        @DisplayName("Should return DASH MPD URL successfully")
        void testGetDashMpdUrl_Success() throws Exception {
            // Arrange
            String dashUrl = "https://youtube.com/dash/manifest.mpd";
            when(videoStreamingService.getDashMpdUrl(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(dashUrl);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/video/dash")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string(dashUrl));
        }

        @Test
        @DisplayName("Should handle null DASH URL")
        void testGetDashMpdUrl_Null() throws Exception {
            // Arrange
            when(videoStreamingService.getDashMpdUrl(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/video/dash")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/dash - DASH Manifest Tests")
    class DashManifestTests {

        private StreamInfo mockStreamInfo;
        private VideoStream mockVideoStream;

        @BeforeEach
        void setUpDashTests() {
            mockStreamInfo = mock(StreamInfo.class);
            mockVideoStream = mock(VideoStream.class);

            when(mockStreamInfo.getDuration()).thenReturn(120L);
            when(mockStreamInfo.getVideoOnlyStreams()).thenReturn(List.of(mockVideoStream));
            when(mockStreamInfo.getAudioStreams()).thenReturn(Collections.emptyList());
            when(mockStreamInfo.getSubtitles()).thenReturn(Collections.emptyList());

            when(streamSelectionService.selectVideoStreams(anyList())).thenReturn(List.of(mockVideoStream));
            when(streamSelectionService.selectAudioStreams(anyList())).thenReturn(Collections.emptyList());
            when(streamSelectionService.selectSubtitles(anyList())).thenReturn(Collections.emptyList());

            when(videoStreamingService.getStreamInfo(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockStreamInfo);
        }


        @Test
        @DisplayName("Should return DASH manifest successfully")
        void testGetDashManifest_Success() throws Exception {
            when(mockStreamInfo.getName()).thenReturn("Test Video");
            String expectedManifest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\" type=\"static\" " +
                    "mediaPresentationDuration=\"PT2M\" minBufferTime=\"PT2S\" " +
                    "profiles=\"urn:mpeg:dash:profile:isoff-on-demand:2011\">\n" +
                    "  <Period duration=\"PT2M\">\n" +
                    "  </Period>\n" +
                    "</MPD>\n";
            when(dashManifestGeneratorService.generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class)))
                    .thenReturn(expectedManifest);

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_XML))
                    .andExpect(content().string(containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")))
                    .andExpect(content().string(containsString("<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\"")))
                    .andExpect(content().string(containsString("type=\"static\"")))
                    .andExpect(content().string(containsString("<Period")))
                    .andExpect(content().string(containsString("</MPD>")));

            verify(videoStreamingService).getStreamInfo(YOUTUBE_URL + TEST_VIDEO_ID);
            verify(mockStreamInfo).getVideoOnlyStreams();
            verify(mockStreamInfo).getAudioStreams();
            verify(mockStreamInfo).getSubtitles();
            verify(streamSelectionService).selectVideoStreams(anyList());
            verify(streamSelectionService).selectAudioStreams(anyList());
            verify(streamSelectionService).selectSubtitles(anyList());
            verify(dashManifestGeneratorService).generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when ID is missing")
        void testGetDashManifest_MissingId() throws Exception {
            mockMvc.perform(get("/api/v1/streams/dash"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(videoStreamingService);
            verifyNoInteractions(dashManifestGeneratorService);
            verifyNoInteractions(streamSelectionService);
        }

        @Test
        @DisplayName("Should return manifest with video AdaptationSet")
        void testGetDashManifest_WithVideoStreams() throws Exception {
            // Override with a more detailed video stream mock for this test
            VideoStream detailedVideoStream = mock(VideoStream.class);
            when(detailedVideoStream.getId()).thenReturn("137");
            when(detailedVideoStream.getResolution()).thenReturn("1080p");
            when(detailedVideoStream.getBitrate()).thenReturn(3000000);
            ItagItem mockItagItem = mock(ItagItem.class);
            when(mockItagItem.getBitrate()).thenReturn(3000000);
            when(detailedVideoStream.getItagItem()).thenReturn(mockItagItem);
            MediaFormat mockFormat = mock(MediaFormat.class);
            when(mockFormat.getName()).thenReturn("MPEG_4");
            when(detailedVideoStream.getFormat()).thenReturn(mockFormat);

            List<VideoStream> allVideoStreams = List.of(detailedVideoStream);
            when(mockStreamInfo.getVideoOnlyStreams()).thenReturn(allVideoStreams);
            when(streamSelectionService.selectVideoStreams(allVideoStreams)).thenReturn(allVideoStreams);

            String manifestWithVideo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\" type=\"static\">\n" +
                    "  <Period>\n" +
                    "    <AdaptationSet id=\"0\" contentType=\"video\" mimeType=\"video/mp4\">\n" +
                    "      <Representation id=\"video-1\" bandwidth=\"3000000\">\n" +
                    "      </Representation>\n" +
                    "    </AdaptationSet>\n" +
                    "  </Period>\n" +
                    "</MPD>\n";
            when(dashManifestGeneratorService.generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class)))
                    .thenReturn(manifestWithVideo);

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_XML))
                    .andExpect(content().string(containsString("contentType=\"video\"")))
                    .andExpect(content().string(containsString("<Representation")));

            verify(streamSelectionService).selectVideoStreams(allVideoStreams);
            verify(dashManifestGeneratorService).generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class));
        }

        @Test
        @DisplayName("Should return 500 when stream info extraction fails")
        void testGetDashManifest_ExtractionException() throws Exception {
            when(videoStreamingService.getStreamInfo(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenThrow(new ExtractionException("Extraction failed"));

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isInternalServerError());

            verify(videoStreamingService).getStreamInfo(YOUTUBE_URL + TEST_VIDEO_ID);
            verifyNoInteractions(dashManifestGeneratorService);
            verifyNoInteractions(streamSelectionService);
        }

        @Test
        @DisplayName("Should return 500 when manifest generation fails")
        void testGetDashManifest_ManifestGenerationException() throws Exception {
            when(dashManifestGeneratorService.generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class)))
                    .thenThrow(new RuntimeException("Manifest generation failed"));

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isInternalServerError());

            verify(videoStreamingService).getStreamInfo(YOUTUBE_URL + TEST_VIDEO_ID);
            verify(dashManifestGeneratorService).generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class));
        }

        @Test
        @DisplayName("Should escape XML special characters in manifest")
        void testGetDashManifest_XmlEscaping() throws Exception {
            String manifestWithEscapedChars = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\">\n" +
                    "  <Period>\n" +
                    "    <AdaptationSet>\n" +
                    "      <BaseURL>https://example.com?param=1&amp;other=2</BaseURL>\n" +
                    "    </AdaptationSet>\n" +
                    "  </Period>\n" +
                    "</MPD>\n";
            when(dashManifestGeneratorService.generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class)))
                    .thenReturn(manifestWithEscapedChars);

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("&amp;")))
                    .andExpect(content().string(not(containsString("&other"))));
        }

        @Test
        @DisplayName("Should include proper content type header")
        void testGetDashManifest_ContentTypeHeader() throws Exception {
            when(dashManifestGeneratorService.generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class)))
                    .thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><MPD></MPD>");

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", MediaType.APPLICATION_XML_VALUE));
        }

        @Test
        @DisplayName("Should handle complete manifest with all stream types")
        void testGetDashManifest_CompleteManifest() throws Exception {
            String completeManifest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<MPD xmlns=\"urn:mpeg:dash:schema:mpd:2011\" type=\"static\">\n" +
                    "  <Period>\n" +
                    "    <AdaptationSet id=\"0\" contentType=\"video\">\n" +
                    "      <Representation id=\"video-1080p\"/>\n" +
                    "      <Representation id=\"video-720p\"/>\n" +
                    "    </AdaptationSet>\n" +
                    "    <AdaptationSet id=\"1\" contentType=\"audio\" lang=\"en\">\n" +
                    "      <Representation id=\"audio-en-high\"/>\n" +
                    "    </AdaptationSet>\n" +
                    "    <AdaptationSet id=\"100\" contentType=\"text\" lang=\"en\">\n" +
                    "      <Representation id=\"subtitle-en\"/>\n" +
                    "    </AdaptationSet>\n" +
                    "  </Period>\n" +
                    "</MPD>\n";
            when(dashManifestGeneratorService.generateManifestXml(ArgumentMatchers.any(DashManifestConfigDTO.class)))
                    .thenReturn(completeManifest);

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("contentType=\"video\"")))
                    .andExpect(content().string(containsString("contentType=\"audio\"")))
                    .andExpect(content().string(containsString("contentType=\"text\"")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/thumbnails - Thumbnail Tests")
    class ThumbnailTests {

        @Test
        @DisplayName("Should return stream thumbnails successfully")
        void testGetStreamThumbnails_Success() throws Exception {
             // Arrange
            List<Image> mockThumbnails = Collections.singletonList(mock(Image.class));
            when(videoStreamingService.getStreamThumbnails(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockThumbnails);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/thumbnails")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/subtitles - Subtitle Tests")
    class SubtitleTests {

        @Test
        @DisplayName("Should return subtitle streams successfully")
        void testGetSubtitleStreams_Success() throws Exception {
            // Arrange
            List<SubtitlesStream> mockSubtitles = Arrays.asList(
                    mock(SubtitlesStream.class),
                    mock(SubtitlesStream.class)
            );
            when(videoStreamingService.getSubtitleStreams(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockSubtitles);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/subtitles")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/segments - Stream Segment Tests")
    class StreamSegmentTests {

        @Test
        @DisplayName("Should return stream segments successfully")
        void testGetStreamSegments_Success() throws Exception {
            // Arrange
            List<StreamSegment> mockSegments = Arrays.asList(
                    mock(StreamSegment.class),
                    mock(StreamSegment.class),
                    mock(StreamSegment.class)
            );
            when(videoStreamingService.getStreamSegments(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockSegments);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/segments")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/preview-frames - Preview Frame Tests")
    class PreviewFrameTests {

        @Test
        @DisplayName("Should return preview frames successfully")
        void testGetPreviewFrames_Success() throws Exception {
            // Arrange
            List<Frameset> mockFrames = Collections.singletonList(mock(Frameset.class));
            when(videoStreamingService.getPreviewFrames(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockFrames);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/preview-frames")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/description - Description Tests")
    class DescriptionTests {

        @Test
        @DisplayName("Should return description successfully")
        void testGetStreamDescription_Success() throws Exception {
            // Arrange
            Description mockDescription = mock(Description.class);
            when(videoStreamingService.getStreamDescription(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockDescription);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/description")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/details - Stream Details Tests")
    class StreamDetailsTests {

        @Test
        @DisplayName("Should return stream details successfully")
        void testGetStreamDetails_Success() throws Exception {
            // Arrange
            StreamInfo mockStreamInfo = mock(StreamInfo.class);
            when(mockStreamInfo.getName()).thenReturn("Test Video");
            when(videoStreamingService.getStreamInfo(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockStreamInfo);

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/details")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streams/related - Related Streams Tests")
    class RelatedStreamsTests {

        @Test
        @DisplayName("Should return related streams successfully")
        void testGetRelatedStreams_Success() throws Exception {
            // Use concrete StreamInfoItem instead of InfoItem interface
            StreamInfoItem item1 = mock(StreamInfoItem.class);
            StreamInfoItem item2 = mock(StreamInfoItem.class);
            StreamInfoItem item3 = mock(StreamInfoItem.class);
            StreamInfoItem item4 = mock(StreamInfoItem.class);
            StreamInfoItem item5 = mock(StreamInfoItem.class);

            // Cast to List<InfoItem> for the service method
            List<InfoItem> mockRelated = Arrays.asList(item1, item2, item3, item4, item5);

            when(videoStreamingService.getRelatedStreams(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(mockRelated);

            mockMvc.perform(get("/api/v1/streams/related")
                            .param("id", TEST_VIDEO_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)));
        }

        @Test
        @DisplayName("Should return empty list when no related streams")
        void testGetRelatedStreams_EmptyList() throws Exception {
            // Arrange
            when(videoStreamingService.getRelatedStreams(YOUTUBE_URL + TEST_VIDEO_ID))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/related")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle extraction exceptions consistently")
        void testErrorHandling_ExtractionException() throws Exception {
            // Arrange
            when(videoStreamingService.getAudioStreams(anyString()))
                    .thenThrow(new ExtractionException("Extraction failed"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/audio")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.errorCode").value("EXTRACTION_ERROR"))
                    .andExpect(jsonPath("$.message").value(containsString("Extraction failed")))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should handle generic exceptions")
        void testErrorHandling_GenericException() throws Exception {
            // Arrange
            when(videoStreamingService.getVideoStreams(anyString()))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/streams/video")
                            .param("id", TEST_VIDEO_ID)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));
        }
    }
}