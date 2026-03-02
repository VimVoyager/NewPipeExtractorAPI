package org.example.api.service;

import org.example.api.exception.ExtractionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.schabi.newpipe.extractor.channel.ChannelInfo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ChannelService.
 * Tests successful extraction, exception wrapping, and URL passthrough.
 */
@DisplayName("ChannelService Tests")
class ChannelServiceTest {

    private ChannelService channelService;

    @BeforeEach
    void setUp() {
        channelService = new ChannelService();
    }

    @Nested
    @DisplayName("getChannelInfo() Tests")
    class GetChannelInfoTests {

        @Test
        @DisplayName("Should return ChannelInfo on successful extraction")
        void testGetChannelInfo_Success() throws Exception {
            // Arrange
            String url = "https://www.youtube.com/@LinusTechTips";
            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);

            try (MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {
                channelInfoMock.when(() -> ChannelInfo.getInfo(url)).thenReturn(mockChannelInfo);

                // Act
                ChannelInfo result = channelService.getChannelInfo(url);

                // Assert
                assertNotNull(result);
                assertSame(mockChannelInfo, result);
            }
        }

        @Test
        @DisplayName("Should pass the URL to ChannelInfo.getInfo() unchanged")
        void testGetChannelInfo_PassesUrlUnchanged() throws Exception {
            // Arrange
            String url = "https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw";
            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);

            try (MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {
                channelInfoMock.when(() -> ChannelInfo.getInfo(url)).thenReturn(mockChannelInfo);

                // Act
                channelService.getChannelInfo(url);

                // Assert
                channelInfoMock.verify(() -> ChannelInfo.getInfo(url));
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException when ChannelInfo.getInfo() throws")
        void testGetChannelInfo_ThrowsExtractionException() {
            // Arrange
            String url = "https://www.youtube.com/@LinusTechTips";
            RuntimeException cause = new RuntimeException("Network error");

            try (MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {
                channelInfoMock.when(() -> ChannelInfo.getInfo(url)).thenThrow(cause);

                // Act & Assert
                ExtractionException ex = assertThrows(ExtractionException.class, () ->
                        channelService.getChannelInfo(url)
                );

                assertEquals("Network error", ex.getMessage());
            }
        }

        @Test
        @DisplayName("Should preserve the original cause in the wrapped ExtractionException")
        void testGetChannelInfo_PreservesCause() {
            // Arrange
            String url = "https://www.youtube.com/@LinusTechTips";
            Throwable rootCause = new IllegalStateException("root cause");
            RuntimeException wrapper = new RuntimeException("outer", rootCause);

            try (MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {
                channelInfoMock.when(() -> ChannelInfo.getInfo(url)).thenThrow(wrapper);

                // Act & Assert
                ExtractionException ex = assertThrows(ExtractionException.class, () ->
                        channelService.getChannelInfo(url)
                );

                assertSame(rootCause, ex.getCause());
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException for handle-style URLs")
        void testGetChannelInfo_HandleUrl_ThrowsOnFailure() {
            // Arrange
            String url = "https://www.youtube.com/@SomeChannel";

            try (MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {
                channelInfoMock.when(() -> ChannelInfo.getInfo(url))
                        .thenThrow(new RuntimeException("Could not resolve handle"));

                // Act & Assert
                assertThrows(ExtractionException.class, () ->
                        channelService.getChannelInfo(url)
                );
            }
        }
    }
}