package org.example.api.controller;

import org.example.api.dto.StreamDetailsDTO;
import org.example.api.dto.dash.DashManifestConfigDTO;
import org.example.api.exception.NoStreamsAvailableException;
import org.example.api.service.DashManifestGeneratorService;
import org.example.api.service.VideoStreamingService;
import org.example.api.service.StreamSelectionService;
import org.example.api.utils.ValidationUtils;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for handling streaming-related API requests for YouTube information.
 * This class provides endpoints for retrieving stream and audio stream data based on provided IDs.
 * All endpoints are prefixed with "/api/v1/streams".
 */
@RestController
@RequestMapping("/api/v1/streams")
public class StreamingController {
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final Logger logger = LoggerFactory.getLogger(StreamingController.class);
    private final VideoStreamingService videoStreamingService;
    private final DashManifestGeneratorService dashManifestGeneratorService;
    private final StreamSelectionService streamSelectionService;

    public StreamingController(VideoStreamingService videoStreamingService, DashManifestGeneratorService dashManifestGeneratorService, StreamSelectionService streamSelectionService) {
        this.videoStreamingService = videoStreamingService;
        this.dashManifestGeneratorService = dashManifestGeneratorService;
        this.streamSelectionService = streamSelectionService;
    }

    /**
     * Builds the watch URL for a video id, validating both.
     *
     */
    private String videoUrl(String id) {
        ValidationUtils.requireNonEmpty(id, "id");
        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);
        return url;
    }

    /**
     * Handles HTTP GET requests to retrieve stream information based on a provided ID.
     *
     * @param id The ID of the stream to retrieve information for.
     * @return 200 with the stream information, 400 for a missing/empty id,
     *         or an error response from the exception handler.
     */
    @GetMapping
    public ResponseEntity<StreamInfo> getStreamInfo(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving stream info for ID: {}", id);

        StreamInfo info = videoStreamingService.getStreamInfo(videoUrl(id));
        return ResponseEntity.ok(info);
    }

    /**
     * Get audio streams for a video.
     */
    @GetMapping("/audio")
    public ResponseEntity<List<AudioStream>> getAudioStreams(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving audio stream for ID: {}", id);

        List<AudioStream> streams = videoStreamingService.getAudioStreams(videoUrl(id));
        return ResponseEntity.ok(streams);
    }

    /**
     * Get video-only streams.
     */
    @GetMapping("/video")
    public ResponseEntity<List<VideoStream>> getVideoStreams(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving video stream for ID: {}", id);

        List<VideoStream> streams = videoStreamingService.getVideoStreams(videoUrl(id));
        return ResponseEntity.ok(streams);
    }

    /**
     * Get DASH MPD URL for adaptive streaming.
     */
    @GetMapping("/video/dash")
    public ResponseEntity<String> getDashMpdUrl(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving DASH MPD URL for ID: {}", id);

        String dashUrl = videoStreamingService.getDashMpdUrl(videoUrl(id));
        return ResponseEntity.ok(dashUrl);
    }

    /**
     * Get DASH MPD XML Manifest for adaptive bitrate streaming
     */
    @GetMapping("/dash")
    public ResponseEntity<String> getDashManifest(@RequestParam(name = "id") String id) {
        logger.debug("Generating optimized DASH manifest for ID: {}", id);

        String url = videoUrl(id);

        StreamInfo streamInfo = videoStreamingService.getStreamInfo(url);

        List<VideoStream> allVideoStreams = streamInfo.getVideoOnlyStreams();
        List<AudioStream> allAudioStreams = streamInfo.getAudioStreams();
        List<SubtitlesStream> allSubtitles = streamInfo.getSubtitles();

        // SABR fallback: if adaptive streams are unavailable, use muxed streams (360p only)
        boolean isMuxedFallback = allVideoStreams.isEmpty();

        if (isMuxedFallback) {
            List<VideoStream> muxedStreams = streamInfo.getVideoStreams();

            if (muxedStreams.isEmpty()) {
                throw new NoStreamsAvailableException(
                        "No streams available — YouTube may be blocking extraction for this video"
                );
            }

            logger.warn("No adaptive streams - falling back to {} muxed stream(s)", muxedStreams.size());

            String directUrl = muxedStreams.getFirst().getContent();
            // TODO: Replace X-STREAM-TYPE header with a standardized header from MDN
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("X-Stream-Type", "muxed-progressive")
                    .body(directUrl);
        }

        // Apply intelligent stream selection
        List<VideoStream> selectedVideoStreams = streamSelectionService.selectVideoStreams(allVideoStreams);
        List<AudioStream> selectedAudioStreams = streamSelectionService.selectAudioStreams(allAudioStreams);
        List<SubtitlesStream> selectedSubtitles = streamSelectionService.selectSubtitles(allSubtitles);

        DashManifestConfigDTO config = DashManifestConfigDTO.fromWithSelectedStreams(
                streamInfo,
                selectedVideoStreams,
                selectedAudioStreams,
                selectedSubtitles
        );

        // Generate manifest XML with selected streams
        String manifest = dashManifestGeneratorService.generateManifestXml(config);

        logger.debug("Generated optimized DASH manifest with {} characters (selected streams: {} video, {} audio, {} subtitle)",
                manifest.length(), selectedVideoStreams.size(), selectedAudioStreams.size(), selectedSubtitles.size());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml"))
                .body(manifest);
    }

    /**
     * Get stream thumbnails
     */
    @GetMapping("/thumbnails")
    public ResponseEntity<List<Image>> getThumbnails(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving stream thumbnails for ID: {}", id);

        List<Image> thumbnails = videoStreamingService.getStreamThumbnails(videoUrl(id));
        return ResponseEntity.ok(thumbnails);
    }

    /**
     * Get subtitle streams.
     */
    @GetMapping("/subtitles")
    public ResponseEntity<List<SubtitlesStream>> getSubtitleStreams(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving subtitle streams for ID: {}", id);

        List<SubtitlesStream> subtitles = videoStreamingService.getSubtitleStreams(videoUrl(id));
        return ResponseEntity.ok(subtitles);
    }

    /**
     * Get stream segments (chapters).
     */
    @GetMapping("/segments")
    public ResponseEntity<List<StreamSegment>> getStreamSegments(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving stream segments for ID: {}", id);

        List<StreamSegment> segments = videoStreamingService.getStreamSegments(videoUrl(id));
        return ResponseEntity.ok(segments);
    }

    /**
     * Get preview frames for video scrubbing.
     */
    @GetMapping("/preview-frames")
    public ResponseEntity<List<Frameset>> getPreviewFrames(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving preview frames stream for ID: {}", id);

        List<Frameset> frames = videoStreamingService.getPreviewFrames(videoUrl(id));
        return ResponseEntity.ok(frames);
    }

    /**
     * Get stream description.
     */
    @GetMapping("/description")
    public ResponseEntity<Description> getStreamDescription(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving description stream for ID: {}", id);

        Description description = videoStreamingService.getStreamDescription(videoUrl(id));
        return ResponseEntity.ok(description);
    }

    /**
     * Get comprehensive stream details (metadata).
     */
    @GetMapping("/details")
    public ResponseEntity<StreamDetailsDTO> getStreamDetails(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving stream details for ID: {}", id);

        StreamInfo info = videoStreamingService.getStreamInfo(videoUrl(id));
        StreamDetailsDTO details = StreamDetailsDTO.from(info);

        return ResponseEntity.ok(details);
    }

    /**
     * Get related videos/streams.
     */
    @GetMapping("/related")
    public ResponseEntity<List<InfoItem>> getRelatedStreams(@RequestParam(name = "id") String id) {
        logger.debug("Retrieving related streams for ID: {}", id);

        List<InfoItem> relatedItems = videoStreamingService.getRelatedStreams(videoUrl(id));
        return ResponseEntity.ok(relatedItems);
    }
}