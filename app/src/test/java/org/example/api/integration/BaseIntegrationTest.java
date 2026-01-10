package org.example.api.integration;

import org.example.api.downloader.DownloaderImpl;
import org.example.api.integration.fixtures.FixtureLoader;
import org.junit.jupiter.api.*;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Tag("integration")
public abstract class BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseIntegrationTest.class);
    private static boolean initialized = false;

    protected static boolean isCI;

    @BeforeAll
    static void init() {
        if (!initialized) {
            logger.info("Initializing test environment");

            // Check if running in CI
            isCI = FixtureLoader.isCI();

            if (isCI) {
                for (String s : Arrays.asList(
                        "========================================",
                        "RUNNING IN CI MODE",
                        "Tests will use fixtures instead of real YouTube API",
                        "Note: Streaming tests are skipped in CI due to YouTube IP blocking")) {
                    logger.warn(s);
                }
                logger.warn("========================================");
            } else {
                logger.info("Running in LOCAL mode - using real YouTube API");
            }

            // Initialize NewPipe
            DownloaderImpl downloader = DownloaderImpl.init(null);
            NewPipe.init(downloader, new Localization("en", "GB"));
            logger.info("Initialized test environment");

            initialized = true;
        }
    }

    @BeforeEach
    void logTestEnvironment() {
        if (isCI) {
            logger.info("Test running in CI mode with fixtures");
        }
    }

    /**
     * Wait for a condition with timeout
     */
    protected void waitFor(long timeoutMs, long pollIntervalMs, java.util.function.BooleanSupplier condition)
            throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new AssertionError("Condition not met within timeout");
            }
            Thread.sleep(pollIntervalMs);
        }
    }

}
