package org.example.api.controller;

import org.example.api.dto.StreamDetailsDTO;
import org.example.api.service.DashManifestGeneratorService;
import org.example.api.service.VideoStreamingService;
import org.example.api.utils.ValidationUtils;
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

    public StreamingController(VideoStreamingService videoStreamingService, DashManifestGeneratorService dashManifestGeneratorService) {
        this.videoStreamingService = videoStreamingService;
        this.dashManifestGeneratorService = dashManifestGeneratorService;
    }

    /**
     * Handles HTTP GET requests to retrieve stream information based on a provided ID.
     *
     * This endpoint requires an ID as a request parameter. It constructs a URL using
     * the ID and interacts with the RestService to fetch stream information.
     * On success, a ResponseEntity with the stream information in JSON format
     * is returned (HTTP 200 OK). If the ID parameter is missing or if an error occurs
     * during retrieval, an appropriate error message is returned (HTTP 400 Bad Request
     * or HTTP 500 Internal Server Error).
     *
     * @param id The ID of the stream to retrieve information for.
     * @return A ResponseEntity object containing either:
     *                           - A success response with the stream information in JSON format if the retrieval is successful (HTTP 200 OK).
     *                           - A bad request response if the ID parameter is missing (HTTP 400 Bad Request).
     *                           - An error response with an appropriate message if an error occurs (HTTP 500 Internal Server Error).
     */
    @GetMapping
    public ResponseEntity<StreamInfo> getStreamInfo(@RequestParam(name = "id") String id) {
        logger.info("Retrieving stream info for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        StreamInfo info = videoStreamingService.getStreamInfo(url);
        return ResponseEntity.ok(info);
    }

    /**
     * Get audio streams for a video.
     */
    @GetMapping("/audio")
    public ResponseEntity<List<AudioStream>> getAudioStreams(@RequestParam(name = "id") String id) {
        logger.info("Retrieving audio stream for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        List<AudioStream> streams = videoStreamingService.getAudioStreams(url);
        return ResponseEntity.ok(streams);
    }

    /**
     * Get video-only streams.
     */
    @GetMapping("/video")
    public ResponseEntity<List<VideoStream>> getVideoStreams(@RequestParam(name = "id") String id) {
        logger.info("Retrieving video stream for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        List<VideoStream> streams = videoStreamingService.getVideoStreams(url);
        return ResponseEntity.ok(streams);
    }

    /**
     * Get DASH MPD URL for adaptive streaming.
     */
    @GetMapping("/video/dash")
    public ResponseEntity<String> getDashMpdUrl(@RequestParam(name = "id") String id) throws Exception {
        logger.info("Retrieving DASH MPD URL for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        String dashUrl = videoStreamingService.getDashMpdUrl(url);
        return ResponseEntity.ok(dashUrl);
    }

    /**
     * Get DASH MPD XML Manifest for adaptive bitrate streaming
     */
    @GetMapping("/dash")
    public ResponseEntity<String> getDashManifest(@RequestParam(name = "id") String id) {
        logger.info("Generating DASH manifest for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        // Get stream information
        StreamInfo streamInfo = videoStreamingService.getStreamInfo(url);

        // Generate DASH manifest XML
        String manifest = dashManifestGeneratorService.generateManifest(streamInfo);

        logger.debug("Generated DASH manifest with {} characters", manifest.length());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(manifest);
    }

    /**
     * Get subtitle streams.
     */
    @GetMapping("/subtitles")
    public ResponseEntity<List<SubtitlesStream>> getSubtitleStreams(@RequestParam(name = "id") String id) {
        logger.info("Retrieving subtitle streams for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        List<SubtitlesStream> subtitles = videoStreamingService.getSubtitleStreams(url);
        return ResponseEntity.ok(subtitles);
    }

    /**
     * Get stream segments (chapters).
     */
    @GetMapping("/segments")
    public ResponseEntity<List<StreamSegment>> getStreamSegments(@RequestParam(name = "id") String id) {
        logger.info("Retrieving stream segments for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        List<StreamSegment> segments = videoStreamingService.getStreamSegments(url);
        return ResponseEntity.ok(segments);
    }

    /**
     * Get preview frames for video scrubbing.
     */
    @GetMapping("/preview-frames")
    public ResponseEntity<List<Frameset>> getPreviewFrames(@RequestParam(name = "id") String id) {
        logger.info("Retrieving preview frames stream for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        List<Frameset> frames = videoStreamingService.getPreviewFrames(url);
        return ResponseEntity.ok(frames);
    }

    /**
     * Get stream description.
     */
    @GetMapping("/description")
    public ResponseEntity<Description> getStreamDescription(@RequestParam(name = "id") String id) {
        logger.info("Retrieving description stream for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        Description description = videoStreamingService.getStreamDescription(url);
        return ResponseEntity.ok(description);
    }

    /**
     * Get comprehensive stream details (metadata).
     */
    @GetMapping("/details")
    public ResponseEntity<StreamDetailsDTO> getStreamDetails(@RequestParam(name = "id") String id) {
        logger.info("Retrieving stream details for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        // Get full stream info
        StreamInfo info = videoStreamingService.getStreamInfo(url);

        // Convert to DTO
        StreamDetailsDTO details = StreamDetailsDTO.from(info);

        return ResponseEntity.ok(details);
    }

    /**
     * Get related videos/streams.
     */
    @GetMapping("/related")
    public ResponseEntity<List<InfoItem>> getRelatedStreams(@RequestParam(name = "id") String id) {
        logger.info("Retrieving related streams for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        List<InfoItem> relatedItems = videoStreamingService.getRelatedStreams(url);
        return ResponseEntity.ok(relatedItems);
    }
}
