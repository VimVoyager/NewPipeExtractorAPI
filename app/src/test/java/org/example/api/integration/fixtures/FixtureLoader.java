package org.example.api.integration.fixtures;

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
            logger.debug("Running locally - will use real New Pipe Extractor");
        }

        return isCi;
    }

    /**
     * Load a fixture file as a String.
     *
     * Supports both JSON and XML fixtures.
     *
     * @param fixturePath Path relative to src/test/resources/fixtures/
     * @return The fixture content as a String
     */
    public static String loadFixture(String fixturePath) throws IOException {
        String fullPath = "/fixtures/%s".formatted(fixturePath);

        logger.debug("Loading fixture: {}", fullPath);

        InputStream is = FixtureLoader.class.getResourceAsStream(fullPath);

        if (is == null) {
            String errorMsg = String.format(
                    "Fixture not found: %s\n" +
                            "Make sure the fixture file exists in src/test/resources/fixtures/\n" +
                            "Expected path: src/test/resources%s",
                    fullPath, fullPath
            );
            throw new IOException(errorMsg);
        }

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        logger.debug("Loaded fixture: {} ({} bytes)", fullPath, content.length());

        return content;
    }

    /**
     * Get the fixture file path for a video ID and endpoint.
     *
     * @param endpoint The endpoint being tested (e.g., "streaminfo", "audio", "dash")
     * @param extension The file extension ("json" or "xml")
     * @return The fixture file path
     */
    public static String getFixturePath(String endpoint, String extension) {
        return String.format("%s.%s", endpoint, extension);
    }

    /**
     * Get the fixture file path for an endpoint (defaults to JSON).
     *
     * @param endpoint The endpoint being tested
     * @return The fixture file path
     */
    public static String getFixturePath(String endpoint) {
        // DASH endpoint returns XML, others return JSON
        if (endpoint
        String extension = "dash".equals(endpoint) ? "xml" : "json";

        return getFixturePath(endpoint, extension);
    }

    /**
     * Check if a fixture exists for a given video ID and endpoint.
     */
    public static boolean fixtureExists(String videoId, String endpoint) {
        String path = "/fixtures/%s".formatted(getFixturePath(videoId, endpoint));
        InputStream is = FixtureLoader.class.getResourceAsStream(path);
        boolean exists = is != null;

        if (exists) {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        return exists;
    }
}