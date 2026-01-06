package org.example.api.integration;

import org.example.api.downloader.DownloaderImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Tag("integration")
public abstract class BaseIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseIntegrationTest.class);
    private static boolean initialized = false;

    @BeforeAll
    static void init() {
        if (!initialized) {
            logger.info("Initializing test environment");
            DownloaderImpl downloader = DownloaderImpl.init(null);
            NewPipe.init(downloader, new Localization("en", "GB"));
            logger.info("Initialized test environment");

            initialized = true;
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
