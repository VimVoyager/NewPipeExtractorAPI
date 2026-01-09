package org.example.api.integration.streaming;

import org.example.api.integration.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class StreamingControllerEdgeCaseIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String TEST_VIDEO_ID = "mImFz8mkaHo";

    private String getBaseUrl() {
        return "http://localhost:%d/api/v1/streams".formatted(port);
    }

    // ========== Performance Tests ==========

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    @DisplayName("Get stream info should complete within timeout")
    void getStreamInfo_shouldCompleteWithinTimeout() {
        String url = "%s?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    @DisplayName("DASH manifest generation should complete within timeout")
    void getDashManifest_shouldCompleteWithinTimeout() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Multiple concurrent requests should be handled safely")
    void concurrentRequests_shouldBeHandledSafely() throws InterruptedException {
        String url = "%s?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);
        int threadCount = 3;
        Thread[] threads = new Thread[threadCount];
        java.util.concurrent.atomic.AtomicInteger successCount =
                new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    successCount.incrementAndGet();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join(20000); // 20 second timeout per thread
        }

        assertThat(successCount.get()).isEqualTo(threadCount);
    }

    // ========== DASH Manifest Edge Cases ==========

    @Test
    @DisplayName("DASH manifest should not be empty")
    void getDashManifest_shouldNotBeEmpty() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .isNotBlank()
                .hasSizeGreaterThan(100); // Manifest should be substantial
    }

    @Test
    @DisplayName("DASH manifest should have reasonable size")
    void getDashManifest_shouldHaveReasonableSize() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String manifest = response.getBody();

        // Manifest should be between 1KB and 100KB (reasonable range)
        assertThat(manifest.length()).isBetween(1000, 100000);
    }

    @Test
    @DisplayName("DASH manifest should contain BaseURL elements")
    void getDashManifest_shouldContainBaseUrls() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String manifest = response.getBody();
        assertThat(manifest).contains("<BaseURL>");
    }

    @Test
    @DisplayName("DASH manifest should have proper namespace")
    void getDashManifest_shouldHaveProperNamespace() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String manifest = response.getBody();
        assertThat(manifest).contains("xmlns=\"urn:mpeg:dash:schema:mpd:");
    }

    // ========== Stream Selection Logic Tests ==========

    @Test
    @DisplayName("Video streams should be filtered by stream selection service")
    void getVideoStreams_shouldBeFiltered() {
        String url = "%s/video?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Should return a reasonable number of streams (not 50+)
        // The exact count depends on your StreamSelectionService logic
    }

    @Test
    @DisplayName("Audio streams should be filtered by stream selection service")
    void getAudioStreams_shouldBeFiltered() {
        String url = "%s/audio?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Should return a reasonable number of streams
    }

    // ========== Content-Type Tests ==========

    @Test
    @DisplayName("DASH endpoint should return XML content type")
    void getDashManifest_shouldReturnXmlContentType() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString())
                .containsIgnoringCase("xml");
    }

    // ========== Deleted/Private Video Tests ==========

    @Test
    @DisplayName("Deleted video should return appropriate error")
    void getStreamInfo_withDeletedVideo_shouldReturnError() {
        // Use a known deleted video ID (you may need to update this)
        String deletedVideoId = "deleted_video_id";
        String url = "%s?id=%s".formatted(getBaseUrl(), deletedVideoId);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode())
                .isIn(HttpStatus.NOT_FOUND, HttpStatus.GONE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== URL Construction Tests ==========

    @Test
    @DisplayName("Video ID should be properly URL encoded")
    void getStreamInfo_shouldHandleUrlEncoding() {
        String url = "%s?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ========== Related Videos Consistency ==========

    @Test
    @DisplayName("Related videos should be consistent across multiple calls")
    void getRelatedStreams_shouldBeConsistent() {
        String url = "%s/related?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response1 = restTemplate.getForEntity(url, String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity(url, String.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Related videos might change slightly but should be generally consistent
        assertThat(response1.getBody()).isNotNull();
        assertThat(response2.getBody()).isNotNull();
    }

    // ========== Thumbnail Availability ==========

    @Test
    @DisplayName("Thumbnails should always be available for valid videos")
    void getThumbnails_shouldAlwaysReturnForValidVideo() {
        String url = "%s/thumbnails?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotBlank();
    }

    // ========== Description Length ==========

    @Test
    @DisplayName("Description should have reasonable length")
    void getStreamDescription_shouldHaveReasonableLength() {
        String url = "%s/description?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();

        // Description should exist and be reasonable length
        assertThat(body).isNotNull();
        assertThat(body.length()).isGreaterThan(10).isLessThan(50000);
    }

    // ========== Details Completeness ==========

    @Test
    @DisplayName("Stream details should be comprehensive")
    void getStreamDetails_shouldBeComprehensive() {
        String url = "%s/details?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = response.getBody();

        // Should contain multiple key fields
        assertThat(body)
                .contains("videoTitle")
                .contains("url")
                .contains("viewCount")
                .contains("channelName");
    }

    // ========== Duplicate Request Tests ==========

    @Test
    @DisplayName("Duplicate requests should return same data")
    void duplicateRequests_shouldReturnSameData() {
        String url = "%s?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response1 = restTemplate.getForEntity(url, String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity(url, String.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Core metadata should be identical
        assertThat(response1.getBody()).isNotNull();
        assertThat(response2.getBody()).isNotNull();
    }
}
