package org.example.api.controller;

import org.example.api.config.GlobalExceptionHandler;
import org.example.api.exception.ExtractionException;
import org.example.api.service.DashManifestGeneratorService;
import org.example.api.service.StreamSelectionService;
import org.example.api.service.VideoStreamingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for StreamingController.
 */
@DisplayName("StreamingController Tests")
class StreamingControllerTest {

    private static final String TEST_VIDEO_ID = "dQw4w9WgXcQ";
    private static final String VIDEO_URL = "https://www.youtube.com/watch?v=" + TEST_VIDEO_ID;

    private MockMvc mockMvc;

    @Mock
    private VideoStreamingService videoStreamingService;

    @Mock
    private DashManifestGeneratorService dashManifestGeneratorService;

    @Mock
    private StreamSelectionService streamSelectionService;

    @InjectMocks
    private StreamingController streamingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(streamingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /** Stubbing or verification against the streaming service. */
    @FunctionalInterface
    interface ServiceInteraction {
        void accept(VideoStreamingService service) throws Exception;
    }

    // ── Accessor endpoints (one table, one test) ─────────────────────────

    static Stream<Arguments> accessorEndpoints() {
        StreamInfo info = mock(StreamInfo.class);
        StreamInfo detailsInfo = mock(StreamInfo.class);
        List<AudioStream> audio = List.of(mock(AudioStream.class));
        List<VideoStream> video = List.of(mock(VideoStream.class));
        String dashUrl = "https://youtube.com/dash/manifest.mpd";
        List<Image> thumbnails = List.of(mock(Image.class));
        List<SubtitlesStream> subtitles = List.of(mock(SubtitlesStream.class));
        List<StreamSegment> segments = List.of(mock(StreamSegment.class));
        List<Frameset> frames = List.of(mock(Frameset.class));
        Description description = mock(Description.class);
        List<InfoItem> related = List.of(mock(StreamInfoItem.class));

        return Stream.of(
                Arguments.of("", (ServiceInteraction)
                                s -> {
                                    when(info.getName()).thenReturn("Test Video");
                                    when(s.getStreamInfo(VIDEO_URL)).thenReturn(info);
                                },
                        (ServiceInteraction) s -> verify(s).getStreamInfo(VIDEO_URL)),
                Arguments.of("/audio", (ServiceInteraction)
                                s -> when(s.getAudioStreams(VIDEO_URL)).thenReturn(audio),
                        (ServiceInteraction) s -> verify(s).getAudioStreams(VIDEO_URL)),
                Arguments.of("/video", (ServiceInteraction)
                                s -> when(s.getVideoStreams(VIDEO_URL)).thenReturn(video),
                        (ServiceInteraction) s -> verify(s).getVideoStreams(VIDEO_URL)),
                Arguments.of("/video/dash", (ServiceInteraction)
                                s -> when(s.getDashMpdUrl(VIDEO_URL)).thenReturn(dashUrl),
                        (ServiceInteraction) s -> verify(s).getDashMpdUrl(VIDEO_URL)),
                Arguments.of("/thumbnails", (ServiceInteraction)
                                s -> when(s.getStreamThumbnails(VIDEO_URL)).thenReturn(thumbnails),
                        (ServiceInteraction) s -> verify(s).getStreamThumbnails(VIDEO_URL)),
                Arguments.of("/subtitles", (ServiceInteraction)
                                s -> when(s.getSubtitleStreams(VIDEO_URL)).thenReturn(subtitles),
                        (ServiceInteraction) s -> verify(s).getSubtitleStreams(VIDEO_URL)),
                Arguments.of("/segments", (ServiceInteraction)
                                s -> when(s.getStreamSegments(VIDEO_URL)).thenReturn(segments),
                        (ServiceInteraction) s -> verify(s).getStreamSegments(VIDEO_URL)),
                Arguments.of("/preview-frames", (ServiceInteraction)
                                s -> when(s.getPreviewFrames(VIDEO_URL)).thenReturn(frames),
                        (ServiceInteraction) s -> verify(s).getPreviewFrames(VIDEO_URL)),
                Arguments.of("/description", (ServiceInteraction)
                                s -> when(s.getStreamDescription(VIDEO_URL)).thenReturn(description),
                        (ServiceInteraction) s -> verify(s).getStreamDescription(VIDEO_URL)),
                Arguments.of("/details", (ServiceInteraction)
                                s -> {
                                    // StreamDetailsDTO.from needs a minimally viable info
                                    when(detailsInfo.getName()).thenReturn("Test Video");
                                    when(detailsInfo.getUrl()).thenReturn(VIDEO_URL);
                                    when(s.getStreamInfo(VIDEO_URL)).thenReturn(detailsInfo);
                                },
                        (ServiceInteraction) s -> verify(s).getStreamInfo(VIDEO_URL)),
                Arguments.of("/related", (ServiceInteraction)
                                s -> when(s.getRelatedStreams(VIDEO_URL)).thenReturn(related),
                        (ServiceInteraction) s -> verify(s).getRelatedStreams(VIDEO_URL)));
    }

    @ParameterizedTest(name = "GET /api/v1/streams{0} delegates with the constructed URL")
    @MethodSource("accessorEndpoints")
    @DisplayName("Accessor endpoints return 200 and delegate with the exact constructed watch URL")
    void accessorEndpoint_delegatesWithConstructedUrl(
            String path, ServiceInteraction stub, ServiceInteraction verification) throws Exception {
        stub.accept(videoStreamingService);

        mockMvc.perform(get("/api/v1/streams" + path)
                        .param("id", TEST_VIDEO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verification.accept(videoStreamingService);
    }

    // ── Parameter binding (all endpoints, missing and empty id) ──────────

    static Stream<String> allEndpointPaths() {
        return Stream.of("", "/audio", "/video", "/video/dash", "/dash",
                "/thumbnails", "/subtitles", "/segments", "/preview-frames",
                "/description", "/details", "/related");
    }

    @ParameterizedTest(name = "GET /api/v1/streams{0} without id returns 400")
    @MethodSource("allEndpointPaths")
    @DisplayName("A missing id parameter returns 400 on every endpoint")
    void missingId_returnsBadRequest(String path) throws Exception {
        mockMvc.perform(get("/api/v1/streams" + path)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "GET /api/v1/streams{0} with empty id returns 400")
    @MethodSource("allEndpointPaths")
    @DisplayName("An empty id parameter returns 400 on every endpoint")
    void emptyId_returnsBadRequest(String path) throws Exception {
        mockMvc.perform(get("/api/v1/streams" + path)
                        .param("id", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(videoStreamingService);
    }

    // ── GET /api/v1/streams/dash ─────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/streams/dash")
    class DashManifestTests {

        private StreamInfo streamInfo;

        @BeforeEach
        void stubStreamInfo() {
            streamInfo = mock(StreamInfo.class);
            when(streamInfo.getDuration()).thenReturn(120L);
            when(videoStreamingService.getStreamInfo(VIDEO_URL)).thenReturn(streamInfo);
        }

        @Test
        @DisplayName("Returns the generated manifest as application/xml, selecting from the extracted streams")
        void returnsGeneratedManifest() throws Exception {
            VideoStream videoStream = mock(VideoStream.class);
            List<VideoStream> videoOnly = List.of(videoStream);
            when(streamInfo.getVideoOnlyStreams()).thenReturn(videoOnly);
            when(streamInfo.getAudioStreams()).thenReturn(Collections.emptyList());
            when(streamInfo.getSubtitles()).thenReturn(Collections.emptyList());

            when(streamSelectionService.selectVideoStreams(videoOnly)).thenReturn(videoOnly);
            when(streamSelectionService.selectAudioStreams(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());
            when(streamSelectionService.selectSubtitles(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());
            when(dashManifestGeneratorService.generateManifestXml(any()))
                    .thenReturn("<MPD/>");

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_XML))
                    .andExpect(content().string("<MPD/>"));

            verify(streamSelectionService).selectVideoStreams(eq(videoOnly));
            verify(streamSelectionService).selectAudioStreams(eq(Collections.emptyList()));
            verify(streamSelectionService).selectSubtitles(eq(Collections.emptyList()));
            verify(dashManifestGeneratorService).generateManifestXml(any());
        }

        @Test
        @DisplayName("Falls back to the muxed stream URL as text/plain when adaptive streams are unavailable")
        void fallsBackToMuxedStream_whenNoAdaptiveStreams() throws Exception {

            String directUrl = "https://rr3---sn-example.googlevideo.com/videoplayback?muxed";
            VideoStream muxed = mock(VideoStream.class);
            when(muxed.getContent()).thenReturn(directUrl);

            when(streamInfo.getVideoOnlyStreams()).thenReturn(Collections.emptyList());
            when(streamInfo.getAudioStreams()).thenReturn(Collections.emptyList());
            when(streamInfo.getSubtitles()).thenReturn(Collections.emptyList());
            when(streamInfo.getVideoStreams()).thenReturn(List.of(muxed));

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                    .andExpect(header().string("X-Stream-Type", "muxed-progressive"))
                    .andExpect(content().string(directUrl));

            // The fallback path must not touch selection or generation.
            verifyNoInteractions(streamSelectionService);
            verifyNoInteractions(dashManifestGeneratorService);
        }

        @Test
        @DisplayName("Returns 503 when neither adaptive nor muxed streams exist")
        void returns503_whenNoStreamsAtAll() throws Exception {
            // Previously untested branch: full SABR blocking.
            when(streamInfo.getVideoOnlyStreams()).thenReturn(Collections.emptyList());
            when(streamInfo.getAudioStreams()).thenReturn(Collections.emptyList());
            when(streamInfo.getSubtitles()).thenReturn(Collections.emptyList());
            when(streamInfo.getVideoStreams()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/streams/dash").param("id", TEST_VIDEO_ID))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(content().string(containsString("No streams available")));
        }
    }

    // ── Exception propagation (single advice-wiring test) ────────────────

    @Test
    @DisplayName("Propagates service failures to the exception handler")
    void propagatesFailuresToAdvice() throws Exception {
        when(videoStreamingService.getStreamInfo(anyString()))
                .thenThrow(new ExtractionException("Extraction failed"));

        mockMvc.perform(get("/api/v1/streams")
                        .param("id", TEST_VIDEO_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("EXTRACTION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("Extraction failed")));
    }
}