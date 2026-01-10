package org.example.api.integration.fixtures;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility for loading test fixtures (pre-recorded API responses).
 *
 * In CI environments (GitHub Actions), tests use fixtures instead of making
 * real requests to YouTube, which blocks datacenter IPs.
 *
 * Locally, tests make real requests to YouTube for accurate testing.
 */
public class FixtureLoader {
    private static final Logger logger = LoggerFactory.getLogger(FixtureLoader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Check if we're running in a CI environment.
     */
    public static boolean isCI() {
        String ci = System.getenv("CI");
        String githubActions = System.getenv("GITHUB_ACTIONS");
        String springProfile = System.getenv("SPRING_PROFILES_ACTIVE");

        boolean isCi = "true".equals(ci) ||
                "true".equals(githubActions) ||
                "ci".equals(springProfile);

        if (isCi) {
            logger.info("Running in CI environment - will use fixtures");
        } else {
            logger.info("Running locally - will use real YouTube API");
        }

        return isCi;
    }

    /**
     * Load a fixture file as a String.
     *
     * @param fixturePath Path relative to src/test/resources/fixtures/
     * @return The fixture content as a String
     */
    public static String loadFixture(String fixturePath) throws IOException {
        String fullPath = "/fixtures/%s".formatted(fixturePath);

        logger.debug("Loading fixture: {}", fullPath);

        InputStream is = FixtureLoader.class.getResourceAsStream(fullPath);

        if (is == null) {
            throw new IOException("Fixture not found: %s\nMake sure the fixture file exists in src/test/resources/fixtures/".formatted(fullPath));
        }

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        logger.debug("Loaded fixture: {} ({} bytes)", fullPath, content.length());

        return content;
    }

    /**
     * Load a fixture file as JSON.
     */
    public static JsonNode loadFixtureAsJson(String fixturePath) throws IOException {
        String content = loadFixture(fixturePath);
        return objectMapper.readTree(content);
    }

    /**
     * Get the fixture path for a video ID.
     *
     * @param videoId The YouTube video ID
     * @param endpoint The endpoint being tested (e.g., "streaminfo", "audio", "dash")
     * @return The fixture file path
     */
    public static String getFixturePath(String videoId, String endpoint) {
        return String.format("%s/%s.json", endpoint, videoId);
    }

    /**
     * Check if a fixture exists for a given video ID and endpoint.
     */
    public static boolean fixtureExists(String videoId, String endpoint) {
        String path = "/fixtures/%s".formatted(getFixturePath(videoId, endpoint));
        InputStream is = FixtureLoader.class.getResourceAsStream(path);
        return is != null;
    }
}