package org.example.api.service;

import org.example.api.exception.ExtractionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.schabi.newpipe.extractor.channel.ChannelInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for ChannelService.
 */
@DisplayName("ChannelService Tests")
class ChannelServiceTest {

    private ChannelService channelService;

    @BeforeEach
    void setUp() {
        channelService = new ChannelService();
    }

    @Test
    @DisplayName("Returns the ChannelInfo extracted for the requested URL")
    void returnsChannelInfoForRequestedUrl() throws Exception {
        String url = "https://www.youtube.com/@LinusTechTips";
        ChannelInfo mockChannelInfo = mock(ChannelInfo.class);

        try (MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {
            channelInfoMock.when(() -> ChannelInfo.getInfo(url)).thenReturn(mockChannelInfo);

            ChannelInfo result = channelService.getChannelInfo(url);

            assertSame(mockChannelInfo, result);
            channelInfoMock.verify(() -> ChannelInfo.getInfo(url));
        }
    }

    @Test
    @DisplayName("Wraps failures in ExtractionException, preserving message and the thrown exception as cause")
    void wrapsFailuresPreservingMessageAndCause() {
        String url = "https://www.youtube.com/@LinusTechTips";
        RuntimeException thrown = new RuntimeException("Network error", new IllegalStateException("root"));

        try (MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {
            channelInfoMock.when(() -> ChannelInfo.getInfo(url)).thenThrow(thrown);

            ExtractionException ex = assertThrows(ExtractionException.class,
                    () -> channelService.getChannelInfo(url));

            assertEquals("Network error", ex.getMessage());
            assertSame(thrown, ex.getCause());
        }
    }
}