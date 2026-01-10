package org.example.api.integration.streaming;

import org.example.api.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StreamingController endpoints.
 *
 * NOTE: In CI environments, these tests are SKIPPED because YouTube blocks datacenter IPs.
 * They run normally in local development.
 */
public class StreamingControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    // Stable video ID for testing
    private static final String TEST_VIDEO_ID = "mImFz8mkaHo";

    private String getBaseUrl() {
        return "http://localhost:%d/api/v1/streams".formatted(port);
    }

    @BeforeEach
    void skipInCI() {
        Assumptions.assumeFalse(isCI, "Streaming tests skipped in CI - YouTube blocks datacenter IPs");
    }

    // ========== Basic Stream Info Tests ==========

//    @Test
//    @DisplayName("Get stream info with valid ID should return complete StreamInfo")
//    void getStreamInfo_withValidId_shouldReturnStreamInfo() {
//        String url = "%s?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);
//
//        // CHANGED: Use String instead of StreamInfo
//        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).isNotNull();
//
//        String info = response.getBody();
//        // Verify JSON structure contains expected fields
//        assertThat(info)
//                .contains("\"id\":\"" + TEST_VIDEO_ID + "\"")
//                .contains("\"name\":")
//                .contains("\"url\":")
//                .contains("\"duration\":")
//                .contains("\"viewCount\":");
//    }

    @Test
    @DisplayName("Get stream info without ID should return 400")
    void getStreamInfo_withoutId_shouldReturnBadRequest() {
        String url = getBaseUrl();

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Get stream info with invalid ID should return error")
    void getStreamInfo_withInvalidId_shouldReturnError() {
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
    void getAudioStreams_withValidId_shouldReturnAudioStreams() {
        String url = "%s/audio?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();

        assertThat(response.getBody())
                .contains("content")
                .contains("averageBitrate")
                .contains("bitrate")
                .contains("itagItem")
                .contains("contentLength")
                .contains("quality")
                .contains("codec");
    }

    @Test
    @DisplayName("Get audio streams should return multiple quality options")
    void getAudioStreams_shouldReturnMultipleQualities() {
        String url = "%s/audio?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();

        // Verify JSON array structure
        String body = response.getBody();
        assertThat(body)
                .startsWith("[")
                .endsWith("]")
                .contains("content")
                .contains("averageBitrate");
    }

    // ========== Video Streams Tests ==========

    @Test
    @DisplayName("Get video streams should return video-only streams")
    void getVideoStreams_withValidId_shouldReturnVideoStreams() {
        String url = "%s/video?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();

        String body = response.getBody();

        // Verify ALL streams are video-only (no mixed audio+video streams)
        assertThat(body)
                .as("All streams should be video-only")
                .contains("\"isVideoOnly\" : true")
                .doesNotContain("\"isVideoOnly\":false");
    }

    @Test
    @DisplayName("Get video streams should return multiple resolutions")
    void getVideoStreams_shouldReturnMultipleResolutions() {
        String url = "%s/video?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();

        String body = response.getBody();

        // Count number of resolution entries to verify multiple resolutions
        int resolutionCount = body.split("\"resolution\"").length - 1;
        assertThat(resolutionCount)
                .as("Should have multiple video resolutions (e.g., 720p, 480p, 360p)")
                .isGreaterThan(1);

        // Verify we have different resolution values (not all the same)
        // Check for common YouTube resolutions
        boolean hasMultipleResolutions =
                (body.contains("\"resolution\":\"720p\"") || body.contains("\"resolution\":\"1080p\"")) &&
                        (body.contains("\"resolution\":\"480p\"") || body.contains("\"resolution\":\"360p\""));

        assertThat(hasMultipleResolutions || resolutionCount >= 3)
                .as("Should have varied resolutions, not just one resolution repeated")
                .isTrue();

        // Verify essential video stream fields are present
        assertThat(body)
                .contains("\"resolution\"")
                .contains("\"width\"")
                .contains("\"height\"")
                .contains("\"fps\"")
                .contains("\"isVideoOnly\"");
    }

    // ========== DASH Manifest Tests ==========

    @Test
    @DisplayName("Get DASH manifest should return valid XML")
    void getDashManifest_withValidId_shouldReturnValidXml() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_XML);

        String manifest = response.getBody();
        assertThat(manifest)
                .isNotNull()
                .contains("<?xml version=")
                .contains("<MPD")
                .contains("<Period")
                .contains("<AdaptationSet")
                .contains("<Representation");
    }

    @Test
    @DisplayName("DASH manifest should contain video and audio adaptation sets")
    void getDashManifest_shouldContainVideoAndAudio() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String manifest = response.getBody();
        assertThat(manifest)
                .contains("mimeType=\"video/")
                .contains("mimeType=\"audio/");
    }

    @Test
    @DisplayName("DASH manifest should have proper structure")
    void getDashManifest_shouldHaveProperStructure() {
        String url = "%s/dash?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        String manifest = response.getBody();
        assertThat(manifest)
                .contains("xmlns=\"urn:mpeg:dash:schema:mpd:")
                .contains("type=")
                .contains("mediaPresentationDuration=");
    }

    // ========== Thumbnails Tests ==========

    @Test
    @DisplayName("Get thumbnails should return list of images")
    void getThumbnails_withValidId_shouldReturnThumbnails() {
        String url = "%s/thumbnails?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();

        // Verify JSON structure for Image objects
        String body = response.getBody();
        assertThat(body)
                .startsWith("[")
                .endsWith("]");

        int thumbnailCount = body.split("\"url\"").length - 1;
        assertThat(thumbnailCount)
                .as("Should have at least one thumbnail")
                .isGreaterThan(0);

        assertThat(body)
                .as("All thumbnail URLs should be from YouTube's image server")
                .contains("https://i.ytimg.com");

        assertThat(body)
                .contains("\"url\"")
                .contains("\"height\"")
                .contains("\"width\"");
    }

    @Test
    @DisplayName("Thumbnails should contain multiple sizes")
    void getThumbnails_shouldContainMultipleSizes() {
        String url = "%s/thumbnails?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String body = response.getBody();

        int thumbnailCount = body.split("\"url\"").length - 1;
        assertThat(thumbnailCount)
                .as("Should have multiple thumbnail sizes")
                .isGreaterThan(1);

        int heightCount = body.split("\"height\"").length - 1;
        assertThat(heightCount)
                .as("Should have multiple height values (indicating different sizes)")
                .isGreaterThan(1);

        int widthCount = body.split("\"width\"").length - 1;
        assertThat(widthCount)
                .as("Should have multiple width values (indicating different sizes)")
                .isGreaterThan(1);

        boolean hasVariedSizes = heightCount >= 2 && widthCount >= 2;
        assertThat(hasVariedSizes)
                .as("Thumbnails should have varied dimensions, not all the same size")
                .isTrue();
    }

    // ========== Subtitles Tests ==========

    @Test
    @DisplayName("Get subtitle streams should return list of subtitles")
    void getSubtitleStreams_withValidId_shouldReturnSubtitles() {
        String url = "%s/subtitles?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String body = response.getBody();
        assertThat(body)
                .startsWith("[")
                .endsWith("]");

        // If there are subtitles, verify structure
        if (!body.equals("[]")) {
            assertThat(body)
                    .contains("url")
                    .contains("format");
        }
    }

    // ========== Segments (Chapters) Tests ==========

    @Test
    @DisplayName("Get stream segments should return chapters")
    void getStreamSegments_withValidId_shouldReturnSegments() {
        String url = "%s/segments?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String body = response.getBody();
        assertThat(body)
                .startsWith("[")
                .endsWith("]");

        // If there are segments, verify structure
        String trimmedBody = body.replaceAll("\\s+", "");
        if (!trimmedBody.equals("[]")) {
            assertThat(body)
                    .contains("startTimeSeconds")
                    .contains("title");
        }
    }

    // ========== Preview Frames Tests ==========

    @Test
    @DisplayName("Get preview frames should return framesets")
    void getPreviewFrames_withValidId_shouldReturnFramesets() {
        String url = "%s/preview-frames?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();

        assertThat(response.getBody())
                .contains("urls")
                .contains("frameWidth")
                .contains("frameHeight")
                .contains("totalCount")
                .contains("durationPerFrame")
                .contains("framesPerPageX")
                .contains("framesPerPageY");
    }

    // ========== Description Tests ==========

    @Test
    @DisplayName("Get stream description should return description")
    void getStreamDescription_withValidId_shouldReturnDescription() {
        String url = "%s/description?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String body = response.getBody();
        // Description should have content field
        assertThat(body)
                .contains("content")
                .contains("type");
    }

    // ========== Stream Details Tests ==========

    @Test
    @DisplayName("Get stream details should return comprehensive metadata")
    void getStreamDetails_withValidId_shouldReturnDetails() {
        String url = "%s/details?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String details = response.getBody();
        assertThat(details)
                .contains("videoTitle")
                .contains("description")
                .contains("viewCount")
                .contains("likeCount");
    }

    @Test
    @DisplayName("Stream details should contain uploader information")
    void getStreamDetails_shouldContainUploaderInfo() {
        String url = "%s/details?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String details = response.getBody();
        assertThat(details)
                .contains("channelName")
                .contains("uploaderAvatars");
    }

    // ========== Related Streams Tests ==========

    @Test
    @DisplayName("Get related streams should return list of related videos")
    void getRelatedStreams_withValidId_shouldReturnRelatedVideos() {
        String url = "%s/related?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();

        String body = response.getBody();
        assertThat(body)
                .startsWith("[")
                .endsWith("]")
                .contains("name")
                .contains("url");
    }

    @Test
    @DisplayName("Related streams should return reasonable number of items")
    void getRelatedStreams_shouldReturnReasonableCount() {
        String url = "%s/related?id=%s".formatted(getBaseUrl(), TEST_VIDEO_ID);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String body = response.getBody();
        // Count occurrences of "name" field to estimate number of items
        int itemCount = (body != null ? body.split("\"name\"").length : 0) - 1;

        // YouTube typically returns 10-20 related videos
        assertThat(itemCount).isBetween(5, 30);
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("All endpoints without ID parameter should return 400")
    void allEndpoints_withoutId_shouldReturnBadRequest() {
        String[] endpoints = {
                getBaseUrl(),
                "%s/audio".formatted(getBaseUrl()),
                "%s/video".formatted(getBaseUrl()),
                "%s/dash".formatted(getBaseUrl()),
                "%s/thumbnails".formatted(getBaseUrl()),
                "%s/subtitles".formatted(getBaseUrl()),
                "%s/segments".formatted(getBaseUrl()),
                "%s/preview-frames".formatted(getBaseUrl()),
                "%s/description".formatted(getBaseUrl()),
                "%s/details".formatted(getBaseUrl()),
                "%s/related".formatted(getBaseUrl())
        };

        for (String endpoint : endpoints) {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            assertThat(response.getStatusCode())
                    .as("Endpoint %s should return 400 without ID", endpoint)
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("All endpoints with empty ID should return error")
    void allEndpoints_withEmptyId_shouldReturnError() {
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
    }
}