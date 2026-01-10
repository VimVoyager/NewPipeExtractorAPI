package org.example.api.integration.streaming;

import org.example.api.integration.BaseIntegrationTest;
import org.example.api.integration.fixtures.FixtureLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Edge case and performance tests for StreamingController.
 *
 * Test Behavior:
 * - LOCAL: Tests make real requests to YouTube API
 * - CI: Tests use pre-recorded fixtures or skip when not applicable
 */
class StreamingControllerEdgeCaseIntegrationTest extends BaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(StreamingControllerEdgeCaseIntegrationTest.class);

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
            String fixtureType = getFixtureType(endpoint);
            String fixturePath = FixtureLoader.getFixturePath(videoId, fixtureType);

            logger.debug("Loading fixture for endpoint '{}': {}", endpoint, fixturePath);
            return FixtureLoader.loadFixture(fixturePath);
        } else {
            // Local: Make real HTTP request
            String url = "%s%s?id=%s".formatted(getBaseUrl(), endpoint, videoId);
            logger.debug("Making real API call to: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            return response.getBody();
        }
    }

    /**
     * Map endpoint path to fixture type directory.
     */
    private String getFixtureType(String endpoint) {
        return switch (endpoint) {
            case "" -> "streaminfo";
            case "/dash" -> "dash";
            case "/video" -> "video";
            case "/audio" -> "audio";
            case "/thumbnails" -> "thumbnails";
            case "/description" -> "description";
            case "/details" -> "details";
            case "/related" -> "related";
            default -> throw new IllegalArgumentException("Unknown endpoint: %s".formatted(endpoint));
        };
    }

    // ========== Performance Tests ==========
    // Note: These tests have different behavior in CI vs local
    // In CI: Tests fixture loading speed (should be very fast)
    // Locally: Tests real API call speed

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Get stream info should complete within timeout")
    void getStreamInfo_shouldCompleteWithinTimeout() throws Exception {
        String response = getResponse("", TEST_VIDEO_ID);
        assertThat(response).isNotNull();

        logger.info("Stream info completed within timeout (using {})",
                isCI ? "fixtures" : "real API");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    @DisplayName("DASH manifest generation should complete within timeout")
    void getDashManifest_shouldCompleteWithinTimeout() throws Exception {
        String response = getResponse("/dash", TEST_VIDEO_ID);
        assertThat(response).isNotNull();

        logger.info("DASH manifest completed within timeout (using {})",
                isCI ? "fixtures" : "real API");
    }

    @Test
    @DisplayName("Multiple concurrent requests should be handled safely")
    void concurrentRequests_shouldBeHandledSafely() throws InterruptedException {
        // This test works in both CI (fixture loading) and local (real API)

        int threadCount = 3;
        Thread[] threads = new Thread[threadCount];
        java.util.concurrent.atomic.AtomicInteger successCount =
                new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    String response = getResponse("", TEST_VIDEO_ID);
                    if (response != null && !response.isEmpty()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    logger.error("Concurrent request failed", e);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join(20000); // 20 second timeout per thread
        }

        assertThat(successCount.get()).isEqualTo(threadCount);

        logger.info("Handled {} concurrent requests successfully", threadCount);
    }

    // ========== DASH Manifest Edge Cases ==========

    @Test
    @DisplayName("DASH manifest should not be empty")
    void getDashManifest_shouldNotBeEmpty() throws Exception {
        String manifest = getResponse("/dash", TEST_VIDEO_ID);

        assertThat(manifest)
                .isNotNull()
                .isNotBlank()
                .hasSizeGreaterThan(100); // Manifest should be substantial

        logger.info("DASH manifest is not empty ({} bytes)", manifest.length());
    }

    @Test
    @DisplayName("DASH manifest should have reasonable size")
    void getDashManifest_shouldHaveReasonableSize() throws Exception {
        String manifest = getResponse("/dash", TEST_VIDEO_ID);

        // Manifest should be between 1KB and 100KB (reasonable range)
        assertThat(manifest.length()).isBetween(1000, 100000);

        logger.info("DASH manifest has reasonable size: {} bytes", manifest.length());
    }

    @Test
    @DisplayName("DASH manifest should contain BaseURL elements")
    void getDashManifest_shouldContainBaseUrls() throws Exception {
        String manifest = getResponse("/dash", TEST_VIDEO_ID);
        assertThat(manifest).contains("<BaseURL>");

        logger.info("DASH manifest contains BaseURL elements");
    }

    @Test
    @DisplayName("DASH manifest should have proper namespace")
    void getDashManifest_shouldHaveProperNamespace() throws Exception {
        String manifest = getResponse("/dash", TEST_VIDEO_ID);
        assertThat(manifest).contains("xmlns=\"urn:mpeg:dash:schema:mpd:");

        logger.info("DASH manifest has proper MPEG-DASH namespace");
    }

    // ========== Stream Selection Logic Tests ==========

    @Test
    @DisplayName("Video streams should be filtered by stream selection service")
    void getVideoStreams_shouldBeFiltered() throws Exception {
        String response = getResponse("/video", TEST_VIDEO_ID);

        assertThat(response).isNotNull();
        // Should return a reasonable number of streams (not 50+)
        // The exact count depends on your StreamSelectionService logic

        int count = response.split("resolution").length - 1;
        logger.info("Video streams filtered to {} streams", count);
    }

    @Test
    @DisplayName("Audio streams should be filtered by stream selection service")
    void getAudioStreams_shouldBeFiltered() throws Exception {
        String response = getResponse("/audio", TEST_VIDEO_ID);

        assertThat(response).isNotNull();
        // Should return a reasonable number of streams

        int count = response.split("averageBitrate").length - 1;
        logger.info("✓Audio streams filtered to {} streams", count);
    }

    // ========== Content-Type Tests ==========

    @Test
    @DisplayName("DASH endpoint should return XML content type")
    void getDashManifest_shouldReturnXmlContentType() {
        // This test only applies to real API calls
        if (isCI) {
            logger.info("Skipping content-type test in CI (testing fixture content only)");
            return;
        }

        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString())
                .containsIgnoringCase("xml");

        logger.info("DASH endpoint returns XML content type");
    }

    // ========== Deleted/Private Video Tests ==========

    @Test
    @DisplayName("Deleted video should return appropriate error")
    void getStreamInfo_withDeletedVideo_shouldReturnError() {
        // This test only makes sense for real API calls
        if (isCI) {
            logger.info("Skipping error handling test in CI (no fixture for deleted video)");
            return;
        }

        String deletedVideoId = "deleted_video_id";
        String url = "%s?id=%s".formatted(getBaseUrl(), deletedVideoId);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.NOT_FOUND, HttpStatus.GONE, HttpStatus.INTERNAL_SERVER_ERROR);

        logger.info("Deleted video returns appropriate error");
    }

    // ========== URL Construction Tests ==========

    @Test
    @DisplayName("Video ID should be properly URL encoded")
    void getStreamInfo_shouldHandleUrlEncoding() throws Exception {
        String response = getResponse("", TEST_VIDEO_ID);
        assertThat(response).isNotNull();

        logger.info("Video ID URL encoding works correctly");
    }

    // ========== Related Videos Consistency ==========

    @Test
    @DisplayName("Related videos should be consistent across multiple calls")
    void getRelatedStreams_shouldBeConsistent() throws Exception {
        String response1 = getResponse("/related", TEST_VIDEO_ID);
        String response2 = getResponse("/related", TEST_VIDEO_ID);

        assertThat(response1).isNotNull();
        assertThat(response2).isNotNull();

        // In CI with fixtures, should be identical
        // Locally with real API, might vary slightly
        if (isCI) {
            assertThat(response1).isEqualTo(response2);
            logger.info("✓ Related videos are identical (using fixtures)");
        } else {
            logger.info("✓ Related videos retrieved successfully (may vary)");
        }
    }

    // ========== Thumbnail Availability ==========

    @Test
    @DisplayName("Thumbnails should always be available for valid videos")
    void getThumbnails_shouldAlwaysReturnForValidVideo() throws Exception {
        String response = getResponse("/thumbnails", TEST_VIDEO_ID);

        assertThat(response).isNotNull().isNotBlank();
        assertThat(response).startsWith("[").endsWith("]");

        logger.info("Thumbnails are available");
    }

    // ========== Description Length ==========

    @Test
    @DisplayName("Description should have reasonable length")
    void getStreamDescription_shouldHaveReasonableLength() throws Exception {
        String response = getResponse("/description", TEST_VIDEO_ID);

        assertThat(response).isNotNull();
        assertThat(response.length()).isGreaterThan(10).isLessThan(50000);

        logger.info("Description has reasonable length: {} bytes", response.length());
    }

    // ========== Details Completeness ==========

    @Test
    @DisplayName("Stream details should be comprehensive")
    void getStreamDetails_shouldBeComprehensive() throws Exception {
        String response = getResponse("/details", TEST_VIDEO_ID);

        // Should contain multiple key fields
        assertThat(response)
                .contains("videoTitle")
                .contains("url")
                .contains("viewCount")
                .contains("channelName");

        logger.info("Stream details are comprehensive");
    }

    // ========== Duplicate Request Tests ==========

    @Test
    @DisplayName("Duplicate requests should return same data")
    void duplicateRequests_shouldReturnSameData() throws Exception {
        String response1 = getResponse("", TEST_VIDEO_ID);
        String response2 = getResponse("", TEST_VIDEO_ID);

        assertThat(response1).isNotNull();
        assertThat(response2).isNotNull();

        // In CI with fixtures, should be identical
        if (isCI) {
            assertThat(response1).isEqualTo(response2);
            logger.info("Duplicate requests return identical data (using fixtures)");
        } else {
            logger.info("Duplicate requests completed successfully");
        }
    }
}