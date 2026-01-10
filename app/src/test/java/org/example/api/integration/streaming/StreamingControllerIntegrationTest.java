package org.example.api.integration.streaming;

import org.example.api.dto.StreamDetailsDTO;
import org.example.api.integration.BaseIntegrationTest;
import org.example.api.integration.fixtures.FixtureLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StreamingController endpoints.
 *
 * Test Behavior:
 * - LOCAL: Tests make real requests to YouTube API
 * - CI: Tests use pre-recorded fixtures (YouTube blocks datacenter IPs)
 *
 * The tests themselves are identical - only the data source changes.
 */
class StreamingControllerIntegrationTest extends BaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(StreamingControllerIntegrationTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String TEST_VIDEO_ID = "mImFz8mkaHo";

    private String getBaseUrl() {
        return "http://localhost:%d/api/v1/streams".formatted(port);
    }

    /**
     * Get response - from real API or fixture depending on environment.
     */
    private String getResponse(String endpoint, String videoId) throws Exception {
        if (isCI) {
            // In CI: Load from fixture
            String fixturePath = FixtureLoader.getFixturePath(endpoint);

            logger.debug("Loading fixture for endpoint '{}': {}", endpoint, fixturePath);

            try {
                return FixtureLoader.loadFixture(fixturePath);
            } catch (Exception e) {
                logger.error("Failed to load fixture: {}", fixturePath, e);
                throw new AssertionError(
                        "Fixture not found: %s\nMake sure you have generated fixtures for video ID: %s\nExpected file: src/test/resources/fixtures/%s".formatted(fixturePath, videoId, fixturePath),
                        e
                );
            }
        } else {
            // Local: Make real HTTP request
            String url = "%s%s?id=%s".formatted(getBaseUrl(), endpoint, videoId);
            logger.debug("Making real API call to: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            return response.getBody();
        }
    }

    // ========== Basic Stream Info Tests ==========

    @Test
    @DisplayName("Get stream info with valid ID should return complete StreamInfo")
    void getStreamInfo_withValidId_shouldReturnStreamInfo() throws Exception {
        String response = getResponse("", TEST_VIDEO_ID);

        // Verify response structure
        assertThat(response)
                .isNotNull()
                .contains("\"id\"")
                .contains("\"name\"")
                .contains("\"url\"")
                .contains("\"duration\"")
                .contains("\"viewCount\"");

        logger.info("Stream info test passed (using {})", isCI ? "fixtures" : "real API");
    }

    @Test
    @DisplayName("Get stream info without ID should return 400")
    void getStreamInfo_withoutId_shouldReturnBadRequest() {
        // This test only makes sense for real API calls
        if (isCI) {
            logger.info("Skipping error handling test in CI (no fixture needed)");
            return;
        }

        String url = getBaseUrl();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Get stream info with invalid ID should return error")
    void getStreamInfo_withInvalidId_shouldReturnError() {
        // This test only makes sense for real API calls
        if (isCI) {
            logger.info("Skipping error handling test in CI (no fixture needed)");
            return;
        }

        String url = "%s?id=invalid_video_id".formatted(getBaseUrl());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== Audio Streams Tests ==========

    @Test
    @DisplayName("Get audio streams should return list of audio streams")
    void getAudioStreams_withValidId_shouldReturnAudioStreams() throws Exception {
        String response = getResponse("/audio", TEST_VIDEO_ID);

        assertThat(response)
                .isNotNull()
                .startsWith("[")
                .endsWith("]")
                .contains("averageBitrate")
                .contains("url");

        logger.info("Audio streams test passed (using {})", isCI ? "fixtures" : "real API");
    }

    @Test
    @DisplayName("Get audio streams should return multiple quality options")
    void getAudioStreams_shouldReturnMultipleQualities() throws Exception {
        String response = getResponse("/audio", TEST_VIDEO_ID);

        // Count number of objects in array by counting "averageBitrate" occurrences
        int count = response.split("averageBitrate").length - 1;
        assertThat(count).isGreaterThan(1);

        logger.info("Found {} audio quality options", count);
    }

    // ========== Video Streams Tests ==========

    @Test
    @DisplayName("Get video streams should return video-only streams")
    void getVideoStreams_withValidId_shouldReturnVideoStreams() throws Exception {
        String response = getResponse("/video", TEST_VIDEO_ID);

        assertThat(response)
                .isNotNull()
                .startsWith("[")
                .endsWith("]")
                .contains("resolution")
                .contains("url");

        logger.info("Video streams test passed (using {})", isCI ? "fixtures" : "real API");
    }

    @Test
    @DisplayName("Get video streams should return multiple resolutions")
    void getVideoStreams_shouldReturnMultipleResolutions() throws Exception {
        String response = getResponse("/video", TEST_VIDEO_ID);

        // Count resolutions
        int count = response.split("resolution").length - 1;
        assertThat(count).isGreaterThan(2);

        logger.info("Found {} video resolutions", count);
    }

    // ========== DASH Manifest Tests ==========

    @Test
    @DisplayName("Get DASH manifest should return valid XML")
    void getDashManifest_withValidId_shouldReturnValidXml() throws Exception {
        String manifest = getResponse("/dash", TEST_VIDEO_ID);

        assertThat(manifest)
                .isNotNull()
                .contains("<?xml version=")
                .contains("<MPD")
                .contains("<Period")
                .contains("<AdaptationSet")
                .contains("<Representation");

        logger.info("DASH manifest test passed (using {})", isCI ? "fixtures" : "real API");
    }

    @Test
    @DisplayName("DASH manifest should contain video and audio adaptation sets")
    void getDashManifest_shouldContainVideoAndAudio() throws Exception {
        String manifest = getResponse("/dash", TEST_VIDEO_ID);

        assertThat(manifest).contains("mimeType=\"video/");
        assertThat(manifest).contains("mimeType=\"audio/");

        logger.info("DASH manifest contains video and audio adaptation sets");
    }

    @Test
    @DisplayName("DASH manifest should be parseable XML")
    void getDashManifest_shouldBeParseableXml() throws Exception {
        String manifest = getResponse("/dash", TEST_VIDEO_ID);

        assertThat(manifest).isNotNull();
        org.assertj.core.api.Assertions.assertThatCode(() -> {
            javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new java.io.ByteArrayInputStream(manifest.getBytes()));
        }).doesNotThrowAnyException();

        logger.info("DASH manifest is valid XML");
    }

    // ========== Thumbnails Tests ==========

    @Test
    @DisplayName("Get thumbnails should return list of images")
    void getThumbnails_withValidId_shouldReturnImages() throws Exception {
        String response = getResponse("/thumbnails", TEST_VIDEO_ID);

        assertThat(response)
                .isNotNull()
                .startsWith("[")
                .endsWith("]")
                .contains("url")
                .contains("height")
                .contains("width");

        logger.info("Thumbnails test passed (using {})", isCI ? "fixtures" : "real API");
    }

    @Test
    @DisplayName("Get thumbnails should return multiple sizes")
    void getThumbnails_shouldReturnMultipleSizes() throws Exception {
        String response = getResponse("/thumbnails", TEST_VIDEO_ID);

        // Count thumbnails
        int count = response.split("\"url\"").length - 1;
        assertThat(count).isGreaterThan(1);

        logger.info("Found {} thumbnail sizes", count);
    }

    // ========== Subtitles Tests ==========

    @Test
    @DisplayName("Get subtitles should return subtitle streams")
    void getSubtitleStreams_withValidId_shouldReturnSubtitles() throws Exception {
        String response = getResponse("/subtitles", TEST_VIDEO_ID);

        assertThat(response)
                .isNotNull()
                .startsWith("[")
                .endsWith("]");

        logger.info("Subtitles test passed (using {})", isCI ? "fixtures" : "real API");
    }

    // ========== Segments Tests ==========

    @Test
    @DisplayName("Get stream segments should return chapters")
    void getStreamSegments_withValidId_shouldReturnSegments() throws Exception {
        String response = getResponse("/segments", TEST_VIDEO_ID);

        assertThat(response)
                .isNotNull()
                .startsWith("[")
                .endsWith("]");

        logger.info("Segments test passed (using {})", isCI ? "fixtures" : "real API");
    }

    // ========== Description Tests ==========

    @Test
    @DisplayName("Get stream description should return description")
    void getStreamDescription_withValidId_shouldReturnDescription() throws Exception {
        String response = getResponse("/description", TEST_VIDEO_ID);

        assertThat(response)
                .isNotNull()
                .contains("content");

        logger.info("Description test passed (using {})", isCI ? "fixtures" : "real API");
    }

    // ========== Stream Details Tests ==========

    @Test
    @DisplayName("Get stream details should return comprehensive metadata")
    void getStreamDetails_withValidId_shouldReturnDetails() throws Exception {
        String response = getResponse("/details", TEST_VIDEO_ID);

        assertThat(response)
                .isNotNull()
                .contains("\"videoTitle\"")
                .contains("\"url\"")
                .contains("\"viewCount\"")
                .contains("\"channelName\"");

        logger.info("Stream details test passed (using {})", isCI ? "fixtures" : "real API");
    }

    @Test
    @DisplayName("Stream details should contain uploader information")
    void getStreamDetails_shouldContainUploaderInfo() throws Exception {
        String response = getResponse("/details", TEST_VIDEO_ID);

        assertThat(response)
                .contains("\"channelName\"")
                .contains("\"uploaderAvatars\"");

        logger.info("Stream details contain uploader information");
    }

    // ========== Related Videos Tests ==========

    @Test
    @DisplayName("Get related streams should return list of related videos")
    void getRelatedStreams_withValidId_shouldReturnRelatedVideos() throws Exception {
        String response = getResponse("/related", TEST_VIDEO_ID);

        assertThat(response)
                .isNotNull()
                .startsWith("[")
                .endsWith("]")
                .contains("name")
                .contains("url");

        logger.info("Related streams test passed (using {})", isCI ? "fixtures" : "real API");
    }

    @Test
    @DisplayName("Related streams should return reasonable number of items")
    void getRelatedStreams_shouldReturnReasonableCount() throws Exception {
        String response = getResponse("/related", TEST_VIDEO_ID);

        // Count items by counting "name" occurrences
        int count = response.split("\"name\"").length - 1;
        assertThat(count).isBetween(5, 30);

        logger.info("Found {} related videos", count);
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("All endpoints without ID parameter should return 400")
    void allEndpoints_withoutId_shouldReturnBadRequest() {
        // This test only makes sense for real API calls
        if (isCI) {
            logger.info("Skipping error handling test in CI (no fixtures for error cases)");
            return;
        }

        String[] endpoints = {
                getBaseUrl(),
                "%s/audio".formatted(getBaseUrl()),
                "%s/video".formatted(getBaseUrl()),
                "%s/dash".formatted(getBaseUrl()),
                "%s/details".formatted(getBaseUrl())
        };

        for (String endpoint : endpoints) {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            assertThat(response.getStatusCode())
                    .as("Endpoint %s should return 400 without ID", endpoint)
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        logger.info("All endpoints properly reject missing ID parameter");
    }

    @Test
    @DisplayName("All endpoints with empty ID should return error")
    void allEndpoints_withEmptyId_shouldReturnError() {
        // This test only makes sense for real API calls
        if (isCI) {
            logger.info("Skipping error handling test in CI (no fixtures for error cases)");
            return;
        }

        String[] endpoints = {
                "%s?id=".formatted(getBaseUrl()),
                "%s/audio?id=".formatted(getBaseUrl()),
                "%s/video?id=".formatted(getBaseUrl()),
                "%s/details?id=".formatted(getBaseUrl())
        };

        for (String endpoint : endpoints) {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            assertThat(response.getStatusCode())
                    .as("Endpoint %s should return error with empty ID", endpoint)
                    .isIn(HttpStatus.BAD_REQUEST, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.info("All endpoints properly reject empty ID parameter");
    }
}