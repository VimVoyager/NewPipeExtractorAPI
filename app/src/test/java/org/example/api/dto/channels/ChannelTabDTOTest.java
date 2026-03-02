package org.example.api.dto.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ChannelTabDTO.
 * Tests factory methods, item mapping, PageDto serialization, and edge cases.
 */
@DisplayName("ChannelTabDTO Tests")
class ChannelTabDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private StreamInfoItem mockStreamItem(String url, String name, String uploaderName,
                                          String uploaderUrl, boolean verified,
                                          long duration, long viewCount, String uploadDate,
                                          boolean isShort) {
        StreamInfoItem item = mock(StreamInfoItem.class);
        when(item.getUrl()).thenReturn(url);
        when(item.getName()).thenReturn(name);
        when(item.getUploaderName()).thenReturn(uploaderName);
        when(item.getUploaderUrl()).thenReturn(uploaderUrl);
        when(item.isUploaderVerified()).thenReturn(verified);
        when(item.getDuration()).thenReturn(duration);
        when(item.getViewCount()).thenReturn(viewCount);
        when(item.getTextualUploadDate()).thenReturn(uploadDate);
        when(item.isShortFormContent()).thenReturn(isShort);
        when(item.getThumbnails()).thenReturn(List.of());
        return item;
    }

    private Page mockPage(String url, byte[] body, List<String> ids) {
        Page page = mock(Page.class);
        when(page.getUrl()).thenReturn(url);
        when(page.getBody()).thenReturn(body);
        when(page.getIds()).thenReturn(ids);
        return page;
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("VideoItemDto Factory Tests")
    class VideoItemDtoFactoryTests {

        @Test
        @DisplayName("Should map all fields from StreamInfoItem")
        void testFrom_AllFields() {
            // Arrange
            Image thumb = mock(Image.class);
            when(thumb.getUrl()).thenReturn("https://i.ytimg.com/thumb.jpg");
            when(thumb.getHeight()).thenReturn(94);
            when(thumb.getWidth()).thenReturn(168);

            StreamInfoItem item = mock(StreamInfoItem.class);
            when(item.getUrl()).thenReturn("https://www.youtube.com/watch?v=abc123");
            when(item.getName()).thenReturn("Test Video");
            when(item.getUploaderName()).thenReturn("Test Channel");
            when(item.getUploaderUrl()).thenReturn("https://www.youtube.com/channel/UCtest");
            when(item.isUploaderVerified()).thenReturn(true);
            when(item.getDuration()).thenReturn(600L);
            when(item.getViewCount()).thenReturn(100_000L);
            when(item.getTextualUploadDate()).thenReturn("3 days ago");
            when(item.isShortFormContent()).thenReturn(false);
            when(item.getThumbnails()).thenReturn(List.of(thumb));

            // Act
            ChannelTabDTO.VideoItemDto dto = ChannelTabDTO.VideoItemDto.from(item);

            // Assert
            assertEquals("https://www.youtube.com/watch?v=abc123", dto.url());
            assertEquals("Test Video", dto.name());
            assertEquals("Test Channel", dto.uploaderName());
            assertEquals("https://www.youtube.com/channel/UCtest", dto.uploaderUrl());
            assertTrue(dto.uploaderVerified());
            assertEquals(600L, dto.duration());
            assertEquals(100_000L, dto.viewCount());
            assertEquals("3 days ago", dto.textualUploadDate());
            assertFalse(dto.isShortFormContent());
            assertEquals(1, dto.thumbnails().size());
            assertEquals("https://i.ytimg.com/thumb.jpg", dto.thumbnails().get(0).url());
            assertEquals(94, dto.thumbnails().get(0).height());
            assertEquals(168, dto.thumbnails().get(0).width());
        }

        @Test
        @DisplayName("Should map multiple thumbnails preserving order")
        void testFrom_MultipleThumbnails() {
            // Arrange
            Image t1 = mock(Image.class);
            when(t1.getUrl()).thenReturn("https://i.ytimg.com/small.jpg");
            when(t1.getHeight()).thenReturn(94);
            when(t1.getWidth()).thenReturn(168);

            Image t2 = mock(Image.class);
            when(t2.getUrl()).thenReturn("https://i.ytimg.com/medium.jpg");
            when(t2.getHeight()).thenReturn(188);
            when(t2.getWidth()).thenReturn(336);

            StreamInfoItem item = mockStreamItem("https://youtube.com/watch?v=x",
                    "Video", "Channel", "https://youtube.com/channel/UC1",
                    false, 300L, 5000L, "1 week ago", false);
            when(item.getThumbnails()).thenReturn(List.of(t1, t2));

            // Act
            ChannelTabDTO.VideoItemDto dto = ChannelTabDTO.VideoItemDto.from(item);

            // Assert
            assertEquals(2, dto.thumbnails().size());
            assertEquals("https://i.ytimg.com/small.jpg", dto.thumbnails().get(0).url());
            assertEquals("https://i.ytimg.com/medium.jpg", dto.thumbnails().get(1).url());
        }

        @Test
        @DisplayName("Should handle unknown view count (-1)")
        void testFrom_UnknownViewCount() {
            // Arrange
            StreamInfoItem item = mockStreamItem("https://youtube.com/watch?v=new",
                    "New Video", "Channel", "https://youtube.com/channel/UC1",
                    false, 120L, -1L, "15 hours ago", false);

            // Act
            ChannelTabDTO.VideoItemDto dto = ChannelTabDTO.VideoItemDto.from(item);

            // Assert
            assertEquals(-1L, dto.viewCount());
        }

        @Test
        @DisplayName("Should handle short-form content flag")
        void testFrom_ShortFormContent() {
            // Arrange
            StreamInfoItem item = mockStreamItem("https://youtube.com/shorts/abc",
                    "My Short", "Shorts Channel", "https://youtube.com/channel/UC2",
                    false, 45L, 500_000L, "2 hours ago", true);

            // Act
            ChannelTabDTO.VideoItemDto dto = ChannelTabDTO.VideoItemDto.from(item);

            // Assert
            assertTrue(dto.isShortFormContent());
        }

        @Test
        @DisplayName("Should handle unverified uploader")
        void testFrom_UnverifiedUploader() {
            // Arrange
            StreamInfoItem item = mockStreamItem("https://youtube.com/watch?v=xyz",
                    "Video", "Small Channel", "https://youtube.com/channel/UC3",
                    false, 900L, 200L, "1 month ago", false);

            // Act
            ChannelTabDTO.VideoItemDto dto = ChannelTabDTO.VideoItemDto.from(item);

            // Assert
            assertFalse(dto.uploaderVerified());
        }
    }

    @Nested
    @DisplayName("PageDto Serialization Tests")
    class PageDtoTests {

        @Test
        @DisplayName("Should build PageDto with all three fields")
        void testBuildNextPage_AllFields() {
            // Arrange
            String url = "https://www.youtube.com/youtubei/v1/browse?prettyPrint=false";
            byte[] body = "{\"continuation\":\"token123\"}".getBytes();
            List<String> ids = List.of("Linus Tech Tips",
                    "https://www.youtube.com/channel/UCXuq",
                    "VERIFIED");
            Page nextPage = mockPage(url, body, ids);

            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of());
            when(tabInfo.getNextPage()).thenReturn(nextPage);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Assert
            assertNotNull(dto.getNextPage());
            assertEquals(url, dto.getNextPage().url());
            assertEquals(Base64.getEncoder().encodeToString(body), dto.getNextPage().body());
            assertEquals(ids, dto.getNextPage().ids());
        }

        @Test
        @DisplayName("Should return null nextPage when Page is null")
        void testBuildNextPage_NullPage() {
            // Arrange
            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of());
            when(tabInfo.getNextPage()).thenReturn(null);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Assert
            assertNull(dto.getNextPage());
        }

        @Test
        @DisplayName("Should return null nextPage when Page has null url")
        void testBuildNextPage_NullUrl() {
            // Arrange
            Page nextPage = mockPage(null, new byte[]{1, 2, 3}, List.of("Channel", "url", "VERIFIED"));

            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of());
            when(tabInfo.getNextPage()).thenReturn(nextPage);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Assert
            assertNull(dto.getNextPage());
        }

        @Test
        @DisplayName("Should set body to null when Page body is null")
        void testBuildNextPage_NullBody() {
            // Arrange
            String url = "https://www.youtube.com/youtubei/v1/browse?prettyPrint=false";
            Page nextPage = mockPage(url, null, List.of("Channel", "https://youtube.com/channel/UC1", "false"));

            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of());
            when(tabInfo.getNextPage()).thenReturn(nextPage);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Assert
            assertNotNull(dto.getNextPage());
            assertEquals(url, dto.getNextPage().url());
            assertNull(dto.getNextPage().body());
        }

        @Test
        @DisplayName("Should Base64-encode the body bytes correctly")
        void testBuildNextPage_BodyEncoding() {
            // Arrange
            byte[] rawBody = "continuation_token_data".getBytes();
            String expectedBase64 = Base64.getEncoder().encodeToString(rawBody);
            Page nextPage = mockPage("https://youtube.com/browse", rawBody, List.of("Ch", "url", "false"));

            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of());
            when(tabInfo.getNextPage()).thenReturn(nextPage);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Assert
            assertEquals(expectedBase64, dto.getNextPage().body());
            // Verify the encoding is reversible
            byte[] decoded = Base64.getDecoder().decode(dto.getNextPage().body());
            assertArrayEquals(rawBody, decoded);
        }
    }

    @Nested
    @DisplayName("from() Factory Tests")
    class FromTabInfoTests {

        @Test
        @DisplayName("Should set tab and channelId from parameters")
        void testFrom_SetsTabAndChannelId() {
            // Arrange
            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of());
            when(tabInfo.getNextPage()).thenReturn(null);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq123");

            // Assert
            assertEquals("videos", dto.getTab());
            assertEquals("UCXuq123", dto.getChannelId());
        }

        @Test
        @DisplayName("Should map StreamInfoItems, skipping non-stream items")
        void testFrom_FiltersNonStreamItems() {
            // Arrange
            StreamInfoItem streamItem = mockStreamItem("https://youtube.com/watch?v=a",
                    "Video A", "Channel", "https://youtube.com/channel/UC1",
                    true, 300L, 1000L, "1 day ago", false);

            // A non-StreamInfoItem (e.g. a ChannelInfoItem) — use a plain InfoItem mock
            InfoItem nonStreamItem = mock(InfoItem.class);

            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of(streamItem, nonStreamItem));
            when(tabInfo.getNextPage()).thenReturn(null);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Assert
            assertEquals(1, dto.getItems().size());
            assertEquals("Video A", dto.getItems().get(0).name());
        }

        @Test
        @DisplayName("Should return empty items list when tab has no items")
        void testFrom_EmptyItems() {
            // Arrange
            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of());
            when(tabInfo.getNextPage()).thenReturn(null);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Assert
            assertNotNull(dto.getItems());
            assertTrue(dto.getItems().isEmpty());
        }

        @Test
        @DisplayName("Should map multiple stream items preserving order")
        void testFrom_MultipleItems() {
            // Arrange
            StreamInfoItem item1 = mockStreamItem("https://youtube.com/watch?v=1",
                    "First Video", "Channel", "url", true, 100L, 1000L, "1 day ago", false);
            StreamInfoItem item2 = mockStreamItem("https://youtube.com/watch?v=2",
                    "Second Video", "Channel", "url", true, 200L, 2000L, "2 days ago", false);
            StreamInfoItem item3 = mockStreamItem("https://youtube.com/watch?v=3",
                    "Third Video", "Channel", "url", true, 300L, 3000L, "3 days ago", false);

            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of(item1, item2, item3));
            when(tabInfo.getNextPage()).thenReturn(null);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Assert
            assertEquals(3, dto.getItems().size());
            assertEquals("First Video", dto.getItems().get(0).name());
            assertEquals("Second Video", dto.getItems().get(1).name());
            assertEquals("Third Video", dto.getItems().get(2).name());
        }
    }

    @Nested
    @DisplayName("fromPage() Factory Tests")
    class FromPageTests {

        @Test
        @DisplayName("Should build DTO from InfoItemsPage with next page")
        @SuppressWarnings("unchecked")
        void testFromPage_WithNextPage() {
            // Arrange
            StreamInfoItem streamItem = mockStreamItem("https://youtube.com/watch?v=p1",
                    "Page 2 Video", "Channel", "https://youtube.com/channel/UC1",
                    false, 500L, 8000L, "5 days ago", false);

            byte[] body = "next_continuation".getBytes();
            List<String> ids = List.of("My Channel", "https://youtube.com/channel/UC1", "false");
            Page nextPage = mockPage("https://youtube.com/browse", body, ids);

            InfoItemsPage<InfoItem> page = mock(InfoItemsPage.class);
            when(page.getItems()).thenReturn(List.of(streamItem));
            when(page.getNextPage()).thenReturn(nextPage);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.fromPage(page, "videos", "UC1");

            // Assert
            assertEquals("videos", dto.getTab());
            assertEquals("UC1", dto.getChannelId());
            assertEquals(1, dto.getItems().size());
            assertEquals("Page 2 Video", dto.getItems().get(0).name());
            assertNotNull(dto.getNextPage());
            assertEquals("https://youtube.com/browse", dto.getNextPage().url());
        }

        @Test
        @DisplayName("Should set nextPage to null on last page")
        @SuppressWarnings("unchecked")
        void testFromPage_LastPage() {
            // Arrange
            InfoItemsPage<InfoItem> page = mock(InfoItemsPage.class);
            when(page.getItems()).thenReturn(List.of());
            when(page.getNextPage()).thenReturn(null);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.fromPage(page, "videos", "UCXuq");

            // Assert
            assertNull(dto.getNextPage());
        }

        @Test
        @DisplayName("Should filter non-stream items from page results")
        @SuppressWarnings("unchecked")
        void testFromPage_FiltersNonStreamItems() {
            // Arrange
            StreamInfoItem streamItem = mockStreamItem("https://youtube.com/watch?v=s",
                    "Stream Item", "Channel", "url", true, 100L, 500L, "1 day ago", false);
            InfoItem nonStreamItem = mock(InfoItem.class);

            InfoItemsPage<InfoItem> page = mock(InfoItemsPage.class);
            when(page.getItems()).thenReturn(List.of(streamItem, nonStreamItem));
            when(page.getNextPage()).thenReturn(null);

            // Act
            ChannelTabDTO dto = ChannelTabDTO.fromPage(page, "videos", "UCXuq");

            // Assert
            assertEquals(1, dto.getItems().size());
            assertEquals("Stream Item", dto.getItems().get(0).name());
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("Should get and set all fields")
        void testGettersSetters() {
            // Arrange
            ChannelTabDTO dto = new ChannelTabDTO();
            List<ChannelTabDTO.VideoItemDto> items = List.of();
            ChannelTabDTO.PageDto page = new ChannelTabDTO.PageDto("url", "body", List.of("a", "b", "c"));

            // Act
            dto.setTab("shorts");
            dto.setChannelId("UCtest123");
            dto.setItems(items);
            dto.setNextPage(page);

            // Assert
            assertEquals("shorts", dto.getTab());
            assertEquals("UCtest123", dto.getChannelId());
            assertSame(items, dto.getItems());
            assertSame(page, dto.getNextPage());
        }

        @Test
        @DisplayName("Should handle null fields")
        void testNullFields() {
            // Arrange
            ChannelTabDTO dto = new ChannelTabDTO();

            // Act
            dto.setTab(null);
            dto.setChannelId(null);
            dto.setItems(null);
            dto.setNextPage(null);

            // Assert
            assertNull(dto.getTab());
            assertNull(dto.getChannelId());
            assertNull(dto.getItems());
            assertNull(dto.getNextPage());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize PageDto with all three fields")
        void testPageDto_Serialization() throws Exception {
            // Arrange
            ChannelTabDTO.PageDto pageDto = new ChannelTabDTO.PageDto(
                    "https://youtube.com/browse",
                    "eyJjb250aW51YXRpb24iOiJ0b2tlbiJ9",
                    List.of("My Channel", "https://youtube.com/channel/UC1", "VERIFIED")
            );

            // Act
            String json = objectMapper.writeValueAsString(pageDto);

            // Assert
            assertTrue(json.contains("\"url\""));
            assertTrue(json.contains("\"body\""));
            assertTrue(json.contains("\"ids\""));
            assertTrue(json.contains("https://youtube.com/browse"));
            assertTrue(json.contains("eyJjb250aW51YXRpb24iOiJ0b2tlbiJ9"));
            assertTrue(json.contains("VERIFIED"));
        }

        @Test
        @DisplayName("Should serialize full ChannelTabDTO")
        void testChannelTabDTO_Serialization() throws Exception {
            // Arrange
            ChannelTabInfo tabInfo = mock(ChannelTabInfo.class);
            when(tabInfo.getRelatedItems()).thenReturn(List.of());
            when(tabInfo.getNextPage()).thenReturn(null);
            ChannelTabDTO dto = ChannelTabDTO.from(tabInfo, "videos", "UCXuq");

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"tab\""));
            assertTrue(json.contains("\"channelId\""));
            assertTrue(json.contains("\"items\""));
            assertTrue(json.contains("videos"));
            assertTrue(json.contains("UCXuq"));
        }
    }
}
