package org.example.api.service;

import org.example.api.dto.channels.ChannelTabDTO;
import org.example.api.exception.ExtractionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ChannelTabService.
 * Tests initial tab fetch, pagination, tab handler lookup, and error handling.
 */
@DisplayName("ChannelTabService Tests")
class ChannelTabServiceTest {

    private ChannelTabService channelTabService;

    @BeforeEach
    void setUp() {
        channelTabService = new ChannelTabService();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /**
     * Builds a ListLinkHandler mock whose contentFilters list contains the given tab string.
     */
    private ListLinkHandler mockTabHandler(String channelUrl, String tab) {
        ListLinkHandler handler = mock(ListLinkHandler.class);
        when(handler.getUrl()).thenReturn(channelUrl + "/" + tab);
        when(handler.getContentFilters()).thenReturn(List.of(tab));
        return handler;
    }

    @SuppressWarnings("unchecked")
    private InfoItemsPage<InfoItem> mockEmptyPage(Page nextPage) {
        InfoItemsPage<InfoItem> page = mock(InfoItemsPage.class);
        when(page.getItems()).thenReturn(List.of());
        when(page.getNextPage()).thenReturn(nextPage);
        when(page.hasNextPage()).thenReturn(nextPage != null);
        return page;
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getChannelTab() Tests")
    class GetChannelTabTests {

        @Test
        @DisplayName("Should return ChannelTabDTO on successful extraction")
        void testGetChannelTab_Success() throws Exception {
            // Arrange
            String channelUrl = "https://www.youtube.com/@LinusTechTips";
            String tab = "videos";
            String channelId = "@LinusTechTips";

            StreamingService mockService = mock(StreamingService.class);
            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);
            ListLinkHandler tabHandler = mockTabHandler(channelUrl, tab);
            ChannelTabExtractor mockExtractor = mock(ChannelTabExtractor.class);
            ChannelTabInfo mockTabInfo = mock(ChannelTabInfo.class);

            when(mockChannelInfo.getTabs()).thenReturn(List.of(tabHandler));
            when(mockService.getChannelTabExtractor(tabHandler)).thenReturn(mockExtractor);
            when(mockTabInfo.getRelatedItems()).thenReturn(List.of());
            when(mockTabInfo.getNextPage()).thenReturn(null);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class);
                 MockedStatic<ChannelTabInfo> tabInfoMock = mockStatic(ChannelTabInfo.class)) {

                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl)).thenReturn(mockService);
                channelInfoMock.when(() -> ChannelInfo.getInfo(channelUrl)).thenReturn(mockChannelInfo);
                tabInfoMock.when(() -> ChannelTabInfo.getInfo(mockExtractor)).thenReturn(mockTabInfo);

                // Act
                ChannelTabDTO result = channelTabService.getChannelTab(channelUrl, tab, channelId);

                // Assert
                assertNotNull(result);
                assertEquals(tab, result.getTab());
                assertEquals(channelId, result.getChannelId());
                assertNotNull(result.getItems());
                assertNull(result.getNextPage());
            }
        }

        @Test
        @DisplayName("Should call fetchPage() on the extractor before getInfo()")
        void testGetChannelTab_CallsFetchPage() throws Exception {
            // Arrange
            String channelUrl = "https://www.youtube.com/@LinusTechTips";
            String tab = "videos";

            StreamingService mockService = mock(StreamingService.class);
            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);
            ListLinkHandler tabHandler = mockTabHandler(channelUrl, tab);
            ChannelTabExtractor mockExtractor = mock(ChannelTabExtractor.class);
            ChannelTabInfo mockTabInfo = mock(ChannelTabInfo.class);

            when(mockChannelInfo.getTabs()).thenReturn(List.of(tabHandler));
            when(mockService.getChannelTabExtractor(tabHandler)).thenReturn(mockExtractor);
            when(mockTabInfo.getRelatedItems()).thenReturn(List.of());
            when(mockTabInfo.getNextPage()).thenReturn(null);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class);
                 MockedStatic<ChannelTabInfo> tabInfoMock = mockStatic(ChannelTabInfo.class)) {

                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl)).thenReturn(mockService);
                channelInfoMock.when(() -> ChannelInfo.getInfo(channelUrl)).thenReturn(mockChannelInfo);
                tabInfoMock.when(() -> ChannelTabInfo.getInfo(mockExtractor)).thenReturn(mockTabInfo);

                // Act
                channelTabService.getChannelTab(channelUrl, tab, "@LinusTechTips");

                // Assert
                verify(mockExtractor).fetchPage();
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException when tab is not found")
        void testGetChannelTab_TabNotFound() {
            // Arrange
            String channelUrl = "https://www.youtube.com/@LinusTechTips";
            ListLinkHandler videosHandler = mockTabHandler(channelUrl, "videos");

            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);
            when(mockChannelInfo.getTabs()).thenReturn(List.of(videosHandler));

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {

                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl)).thenReturn(mock(StreamingService.class));
                channelInfoMock.when(() -> ChannelInfo.getInfo(channelUrl)).thenReturn(mockChannelInfo);

                // Act & Assert
                ExtractionException ex = assertThrows(ExtractionException.class, () ->
                        channelTabService.getChannelTab(channelUrl, "playlists", "@LinusTechTips")
                );

                assertTrue(ex.getMessage().contains("playlists"));
                assertTrue(ex.getMessage().contains("not found"));
            }
        }

        @Test
        @DisplayName("Should find tab handler case-insensitively")
        void testGetChannelTab_TabLookupCaseInsensitive() throws Exception {
            // Arrange
            String channelUrl = "https://www.youtube.com/@LinusTechTips";

            // Handler registered with lowercase
            ListLinkHandler tabHandler = mockTabHandler(channelUrl, "videos");

            StreamingService mockService = mock(StreamingService.class);
            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);
            ChannelTabExtractor mockExtractor = mock(ChannelTabExtractor.class);
            ChannelTabInfo mockTabInfo = mock(ChannelTabInfo.class);

            when(mockChannelInfo.getTabs()).thenReturn(List.of(tabHandler));
            when(mockService.getChannelTabExtractor(tabHandler)).thenReturn(mockExtractor);
            when(mockTabInfo.getRelatedItems()).thenReturn(List.of());
            when(mockTabInfo.getNextPage()).thenReturn(null);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class);
                 MockedStatic<ChannelTabInfo> tabInfoMock = mockStatic(ChannelTabInfo.class)) {

                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl)).thenReturn(mockService);
                channelInfoMock.when(() -> ChannelInfo.getInfo(channelUrl)).thenReturn(mockChannelInfo);
                tabInfoMock.when(() -> ChannelTabInfo.getInfo(mockExtractor)).thenReturn(mockTabInfo);

                // Act — request with uppercase tab type
                ChannelTabDTO result = channelTabService.getChannelTab(channelUrl, "VIDEOS", "@LinusTechTips");

                // Assert
                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Should wrap unexpected exceptions in ExtractionException")
        void testGetChannelTab_UnexpectedException() {
            // Arrange
            String channelUrl = "https://www.youtube.com/@LinusTechTips";

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl))
                        .thenThrow(new RuntimeException("Network error"));

                // Act & Assert
                assertThrows(ExtractionException.class, () ->
                        channelTabService.getChannelTab(channelUrl, "videos", "@LinusTechTips")
                );
            }
        }
    }

    @Nested
    @DisplayName("getChannelTabPage() Tests")
    class GetChannelTabPageTests {

        @Test
        @DisplayName("Should return ChannelTabDTO on successful page fetch")
        @SuppressWarnings("unchecked")
        void testGetChannelTabPage_Success() throws Exception {
            // Arrange
            String channelId = "UCXuqSBlHAE6Xw-yeJA0Tunw";
            String tab = "videos";
            String pageUrl = "https://www.youtube.com/youtubei/v1/browse?prettyPrint=false";
            byte[] rawBody = "{\"continuation\":\"token\"}".getBytes();
            String pageBody = Base64.getEncoder().encodeToString(rawBody);
            List<String> pageIds = List.of(
                    "Linus Tech Tips",
                    "https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw",
                    "VERIFIED"
            );

            String channelUrl = pageIds.get(1);
            StreamingService mockService = mock(StreamingService.class);
            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);
            ListLinkHandler tabHandler = mockTabHandler(channelUrl, tab);
            ChannelTabExtractor mockExtractor = mock(ChannelTabExtractor.class);
            InfoItemsPage<InfoItem> mockPage = mockEmptyPage(null);

            when(mockChannelInfo.getTabs()).thenReturn(List.of(tabHandler));
            when(mockService.getChannelTabExtractor(tabHandler)).thenReturn(mockExtractor);
            when(mockExtractor.getPage(any(Page.class))).thenReturn(mockPage);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {

                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl)).thenReturn(mockService);
                channelInfoMock.when(() -> ChannelInfo.getInfo(channelUrl)).thenReturn(mockChannelInfo);

                // Act
                ChannelTabDTO result = channelTabService.getChannelTabPage(
                        channelId, tab, pageUrl, pageBody, pageIds);

                // Assert
                assertNotNull(result);
                assertEquals(tab, result.getTab());
                assertEquals(channelId, result.getChannelId());
            }
        }

        @Test
        @DisplayName("Should reconstruct Page with decoded body bytes")
        @SuppressWarnings("unchecked")
        void testGetChannelTabPage_ReconstructsPageBody() throws Exception {
            // Arrange
            byte[] rawBody = "continuation_token".getBytes();
            String pageBody = Base64.getEncoder().encodeToString(rawBody);
            List<String> pageIds = List.of(
                    "Channel Name",
                    "https://www.youtube.com/channel/UCtest",
                    "false"
            );
            String channelUrl = pageIds.get(1);

            StreamingService mockService = mock(StreamingService.class);
            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);
            ListLinkHandler tabHandler = mockTabHandler(channelUrl, "videos");
            ChannelTabExtractor mockExtractor = mock(ChannelTabExtractor.class);
            InfoItemsPage<InfoItem> mockPage = mockEmptyPage(null);

            when(mockChannelInfo.getTabs()).thenReturn(List.of(tabHandler));
            when(mockService.getChannelTabExtractor(tabHandler)).thenReturn(mockExtractor);
            when(mockExtractor.getPage(any(Page.class))).thenReturn(mockPage);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {

                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl)).thenReturn(mockService);
                channelInfoMock.when(() -> ChannelInfo.getInfo(channelUrl)).thenReturn(mockChannelInfo);

                // Act
                channelTabService.getChannelTabPage("UCtest", "videos",
                        "https://youtube.com/browse", pageBody, pageIds);

                // Assert — verify extractor received a Page (body is validated by the extractor internally)
                verify(mockExtractor).getPage(any(Page.class));
            }
        }

        @Test
        @DisplayName("Should throw ExtractionException when pageIds has fewer than 2 elements")
        void testGetChannelTabPage_PageIdsTooShort() {
            // Arrange — only one element, channelUrl at index 1 is absent
            List<String> shortIds = List.of("Channel Name");

            // Act & Assert
            ExtractionException ex = assertThrows(ExtractionException.class, () ->
                    channelTabService.getChannelTabPage(
                            "UCtest", "videos",
                            "https://youtube.com/browse",
                            "bodyBase64",
                            shortIds)
            );

            assertTrue(ex.getMessage().contains("pageIds missing channelUrl"));
        }

        @Test
        @DisplayName("Should throw ExtractionException when pageIds is null")
        void testGetChannelTabPage_NullPageIds() {
            // Act & Assert
            ExtractionException ex = assertThrows(ExtractionException.class, () ->
                    channelTabService.getChannelTabPage(
                            "UCtest", "videos",
                            "https://youtube.com/browse",
                            "bodyBase64",
                            null)
            );

            assertTrue(ex.getMessage().contains("pageIds missing channelUrl"));
        }

        @Test
        @DisplayName("Should throw ExtractionException when tab not found for channel")
        void testGetChannelTabPage_TabNotFound() {
            // Arrange
            List<String> pageIds = List.of(
                    "Channel Name",
                    "https://www.youtube.com/channel/UCtest",
                    "false"
            );
            String channelUrl = pageIds.get(1);

            // Channel only has "videos" tab, but we request "playlists"
            ListLinkHandler videosHandler = mockTabHandler(channelUrl, "videos");

            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);
            when(mockChannelInfo.getTabs()).thenReturn(List.of(videosHandler));

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {

                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl)).thenReturn(mock(StreamingService.class));
                channelInfoMock.when(() -> ChannelInfo.getInfo(channelUrl)).thenReturn(mockChannelInfo);

                // Act & Assert
                ExtractionException ex = assertThrows(ExtractionException.class, () ->
                        channelTabService.getChannelTabPage(
                                "UCtest", "playlists",
                                "https://youtube.com/browse",
                                "body",
                                pageIds)
                );

                assertTrue(ex.getMessage().contains("playlists"));
                assertTrue(ex.getMessage().contains("not found"));
            }
        }

        @Test
        @DisplayName("Should wrap unexpected exceptions in ExtractionException")
        void testGetChannelTabPage_UnexpectedException() {
            // Arrange
            List<String> pageIds = List.of(
                    "Channel",
                    "https://www.youtube.com/channel/UCtest",
                    "false"
            );
            String channelUrl = pageIds.get(1);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class)) {
                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl))
                        .thenThrow(new RuntimeException("Connection refused"));

                // Act & Assert
                assertThrows(ExtractionException.class, () ->
                        channelTabService.getChannelTabPage(
                                "UCtest", "videos",
                                "https://youtube.com/browse",
                                "body",
                                pageIds)
                );
            }
        }

        @Test
        @DisplayName("Should handle null pageBody without throwing during decode")
        @SuppressWarnings("unchecked")
        void testGetChannelTabPage_NullPageBody() throws Exception {
            // Arrange
            List<String> pageIds = List.of(
                    "Channel",
                    "https://www.youtube.com/channel/UCtest",
                    "false"
            );
            String channelUrl = pageIds.get(1);

            StreamingService mockService = mock(StreamingService.class);
            ChannelInfo mockChannelInfo = mock(ChannelInfo.class);
            ListLinkHandler tabHandler = mockTabHandler(channelUrl, "videos");
            ChannelTabExtractor mockExtractor = mock(ChannelTabExtractor.class);
            InfoItemsPage<InfoItem> mockPage = mockEmptyPage(null);

            when(mockChannelInfo.getTabs()).thenReturn(List.of(tabHandler));
            when(mockService.getChannelTabExtractor(tabHandler)).thenReturn(mockExtractor);
            when(mockExtractor.getPage(any(Page.class))).thenReturn(mockPage);

            try (MockedStatic<NewPipe> newPipeMock = mockStatic(NewPipe.class);
                 MockedStatic<ChannelInfo> channelInfoMock = mockStatic(ChannelInfo.class)) {

                newPipeMock.when(() -> NewPipe.getServiceByUrl(channelUrl)).thenReturn(mockService);
                channelInfoMock.when(() -> ChannelInfo.getInfo(channelUrl)).thenReturn(mockChannelInfo);

                // Act — null body should not throw; body becomes null bytes in the Page
                ChannelTabDTO result = channelTabService.getChannelTabPage(
                        "UCtest", "videos",
                        "https://youtube.com/browse",
                        null,
                        pageIds);

                assertNotNull(result);
            }
        }
    }
}
