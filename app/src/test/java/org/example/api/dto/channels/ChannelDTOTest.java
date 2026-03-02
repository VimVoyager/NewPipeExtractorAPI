package org.example.api.dto.channels;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for ChannelDTO.
 * Tests factory method mapping, image/tab handling, URL derivation, and edge cases.
 */
@DisplayName("ChannelDTO Tests")
class ChannelDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private Image mockImage(String url, int height, int width, Image.ResolutionLevel level) {
        Image img = mock(Image.class);
        when(img.getUrl()).thenReturn(url);
        when(img.getHeight()).thenReturn(height);
        when(img.getWidth()).thenReturn(width);
        when(img.getEstimatedResolutionLevel()).thenReturn(level);
        return img;
    }

    private ListLinkHandler mockTab(String url, String originalUrl, String id,
                                    List<String> contentFilters, String sortFilter) {
        ListLinkHandler tab = mock(ListLinkHandler.class);
        when(tab.getUrl()).thenReturn(url);
        when(tab.getOriginalUrl()).thenReturn(originalUrl);
        when(tab.getId()).thenReturn(id);
        when(tab.getContentFilters()).thenReturn(contentFilters);
        when(tab.getSortFilter()).thenReturn(sortFilter);
        return tab;
    }

    private ChannelInfo buildFullChannelInfo() {
        ChannelInfo info = mock(ChannelInfo.class);
        when(info.getServiceId()).thenReturn(0);
        when(info.getId()).thenReturn("UCXuqSBlHAE6Xw-yeJA0Tunw");
        when(info.getUrl()).thenReturn("https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw");
        when(info.getOriginalUrl()).thenReturn("https://www.youtube.com/@LinusTechTips");
        when(info.getName()).thenReturn("Linus Tech Tips");
        when(info.getParentChannelName()).thenReturn(null);
        when(info.getParentChannelUrl()).thenReturn(null);
        when(info.getFeedUrl()).thenReturn("https://www.youtube.com/feeds/videos.xml?channel_id=UCXuq");
        when(info.getSubscriberCount()).thenReturn(15_000_000L);
        when(info.getDescription()).thenReturn("Tech channel");
        when(info.isVerified()).thenReturn(true);
        when(info.getAvatars()).thenReturn(List.of());
        when(info.getBanners()).thenReturn(List.of());
        when(info.getParentChannelAvatars()).thenReturn(List.of());
        when(info.getTabs()).thenReturn(List.of());
        when(info.getTags()).thenReturn(List.of());
        return info;
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("from() Factory Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should map all scalar fields from ChannelInfo")
        void testFrom_AllScalarFields() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(0, dto.getServiceId());
            assertEquals("UCXuqSBlHAE6Xw-yeJA0Tunw", dto.getId());
            assertEquals("https://www.youtube.com/channel/UCXuqSBlHAE6Xw-yeJA0Tunw", dto.getUrl());
            assertEquals("https://www.youtube.com/@LinusTechTips", dto.getOriginalUrl());
            assertEquals("Linus Tech Tips", dto.getName());
            assertNull(dto.getParentChannelName());
            assertNull(dto.getParentChannelUrl());
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCXuq", dto.getFeedUrl());
            assertEquals(15_000_000L, dto.getSubscriberCount());
            assertEquals("Tech channel", dto.getDescription());
            assertTrue(dto.isVerified());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when ChannelInfo is null")
        void testFrom_NullChannelInfo() {
            assertThrows(IllegalArgumentException.class, () -> ChannelDTO.from(null));
        }

        @Test
        @DisplayName("Should always set errors to empty list")
        void testFrom_ErrorsIsEmptyList() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertNotNull(dto.getErrors());
            assertTrue(dto.getErrors().isEmpty());
        }

        @Test
        @DisplayName("Should map tags from ChannelInfo")
        void testFrom_Tags() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();
            when(info.getTags()).thenReturn(List.of("tech", "linux", "hardware"));

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(3, dto.getTags().size());
            assertEquals("tech", dto.getTags().get(0));
            assertEquals("linux", dto.getTags().get(1));
            assertEquals("hardware", dto.getTags().get(2));
        }
    }

    @Nested
    @DisplayName("Image Mapping Tests")
    class ImageMappingTests {

        @Test
        @DisplayName("Should map avatar images with all fields")
        void testFrom_AvatarImages() {
            // Arrange
            Image avatar = mockImage("https://yt3.ggpht.com/avatar.jpg", 88, 88,
                    Image.ResolutionLevel.MEDIUM);
            ChannelInfo info = buildFullChannelInfo();
            when(info.getAvatars()).thenReturn(List.of(avatar));

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(1, dto.getAvatars().size());
            ChannelDTO.ImageDto img = dto.getAvatars().get(0);
            assertEquals("https://yt3.ggpht.com/avatar.jpg", img.url());
            assertEquals(88, img.height());
            assertEquals(88, img.width());
            assertEquals("MEDIUM", img.estimatedResolutionLevel());
        }

        @Test
        @DisplayName("Should map banner images")
        void testFrom_BannerImages() {
            // Arrange
            Image banner = mockImage("https://yt3.ggpht.com/banner.jpg", 1080, 2560,
                    Image.ResolutionLevel.HIGH);
            ChannelInfo info = buildFullChannelInfo();
            when(info.getBanners()).thenReturn(List.of(banner));

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(1, dto.getBanners().size());
            assertEquals("https://yt3.ggpht.com/banner.jpg", dto.getBanners().get(0).url());
            assertEquals(1080, dto.getBanners().get(0).height());
            assertEquals("HIGH", dto.getBanners().get(0).estimatedResolutionLevel());
        }

        @Test
        @DisplayName("Should map parent channel avatars")
        void testFrom_ParentChannelAvatars() {
            // Arrange
            Image parentAvatar = mockImage("https://yt3.ggpht.com/parent.jpg", 68, 68,
                    Image.ResolutionLevel.LOW);
            ChannelInfo info = buildFullChannelInfo();
            when(info.getParentChannelAvatars()).thenReturn(List.of(parentAvatar));

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(1, dto.getParentChannelAvatars().size());
            assertEquals("https://yt3.ggpht.com/parent.jpg", dto.getParentChannelAvatars().get(0).url());
            assertEquals("LOW", dto.getParentChannelAvatars().get(0).estimatedResolutionLevel());
        }

        @Test
        @DisplayName("Should return empty lists when no images present")
        void testFrom_EmptyImageLists() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertNotNull(dto.getAvatars());
            assertTrue(dto.getAvatars().isEmpty());
            assertNotNull(dto.getBanners());
            assertTrue(dto.getBanners().isEmpty());
            assertNotNull(dto.getParentChannelAvatars());
            assertTrue(dto.getParentChannelAvatars().isEmpty());
        }

        @Test
        @DisplayName("Should map multiple avatars preserving order")
        void testFrom_MultipleAvatars() {
            // Arrange
            Image small = mockImage("https://yt3.ggpht.com/small.jpg", 32, 32, Image.ResolutionLevel.LOW);
            Image large = mockImage("https://yt3.ggpht.com/large.jpg", 512, 512, Image.ResolutionLevel.HIGH);
            ChannelInfo info = buildFullChannelInfo();
            when(info.getAvatars()).thenReturn(List.of(small, large));

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(2, dto.getAvatars().size());
            assertEquals("https://yt3.ggpht.com/small.jpg", dto.getAvatars().get(0).url());
            assertEquals("https://yt3.ggpht.com/large.jpg", dto.getAvatars().get(1).url());
        }
    }

    @Nested
    @DisplayName("Tab Mapping Tests")
    class TabMappingTests {

        @Test
        @DisplayName("Should map tab with all fields")
        void testFrom_TabAllFields() {
            // Arrange
            ListLinkHandler tab = mockTab(
                    "https://www.youtube.com/channel/UCXuq/videos",
                    "https://www.youtube.com/@LinusTechTips/videos",
                    "videos",
                    List.of("videos"),
                    ""
            );
            ChannelInfo info = buildFullChannelInfo();
            when(info.getTabs()).thenReturn(List.of(tab));

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(1, dto.getTabs().size());
            ChannelDTO.TabDto tabDto = dto.getTabs().get(0);
            assertEquals("https://www.youtube.com/channel/UCXuq/videos", tabDto.url());
            assertEquals("https://www.youtube.com/@LinusTechTips/videos", tabDto.originalUrl());
            assertEquals("videos", tabDto.id());
            assertEquals(List.of("videos"), tabDto.contentFilters());
            assertEquals("", tabDto.sortFilter());
            assertEquals("https://www.youtube.com", tabDto.baseUrl());
        }

        @Test
        @DisplayName("Should extract baseUrl correctly from tab URL")
        void testFrom_BaseUrlExtraction() {
            // Arrange
            ListLinkHandler tab = mockTab(
                    "https://www.youtube.com/channel/UCXuq/shorts",
                    "https://www.youtube.com/@LinusTechTips/shorts",
                    "shorts", List.of("shorts"), ""
            );
            ChannelInfo info = buildFullChannelInfo();
            when(info.getTabs()).thenReturn(List.of(tab));

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals("https://www.youtube.com", dto.getTabs().get(0).baseUrl());
        }

//        @Test
//        @DisplayName("Should fall back to https:// for blank tab URL")
//        void testFrom_BaseUrlFallback_BlankUrl() {
//            // Arrange
//            ListLinkHandler tab = mockTab("", "", "tab", List.of(), "");
//            ChannelInfo info = buildFullChannelInfo();
//            when(info.getTabs()).thenReturn(List.of(tab));
//
//            // Act
//            ChannelDTO dto = ChannelDTO.from(info);
//
//            // Assert
//            assertEquals("https://", dto.getTabs().get(0).baseUrl());
//        }
//
//        @Test
//        @DisplayName("Should fall back to https:// for null tab URL")
//        void testFrom_BaseUrlFallback_NullUrl() {
//            // Arrange
//            ListLinkHandler tab = mockTab(null, null, "tab", List.of(), "");
//            ChannelInfo info = buildFullChannelInfo();
//            when(info.getTabs()).thenReturn(List.of(tab));
//
//            // Act
//            ChannelDTO dto = ChannelDTO.from(info);
//
//            // Assert
//            assertEquals("https://", dto.getTabs().get(0).baseUrl());
//        }

        @Test
        @DisplayName("Should map multiple tabs preserving order")
        void testFrom_MultipleTabs() {
            // Arrange
            ListLinkHandler videos = mockTab("https://youtube.com/channel/UC1/videos",
                    "url", "videos", List.of("videos"), "");
            ListLinkHandler shorts = mockTab("https://youtube.com/channel/UC1/shorts",
                    "url", "shorts", List.of("shorts"), "");
            ListLinkHandler live = mockTab("https://youtube.com/channel/UC1/streams",
                    "url", "livestreams", List.of("livestreams"), "");

            ChannelInfo info = buildFullChannelInfo();
            when(info.getTabs()).thenReturn(List.of(videos, shorts, live));

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(3, dto.getTabs().size());
            assertEquals("videos", dto.getTabs().get(0).id());
            assertEquals("shorts", dto.getTabs().get(1).id());
            assertEquals("livestreams", dto.getTabs().get(2).id());
        }

        @Test
        @DisplayName("Should return empty tabs list when channel has no tabs")
        void testFrom_EmptyTabs() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();
            when(info.getTabs()).thenReturn(List.of());

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertNotNull(dto.getTabs());
            assertTrue(dto.getTabs().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle hidden subscriber count (-1)")
        void testFrom_HiddenSubscriberCount() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();
            when(info.getSubscriberCount()).thenReturn(-1L);

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(-1L, dto.getSubscriberCount());
        }

        @Test
        @DisplayName("Should handle very large subscriber count")
        void testFrom_LargeSubscriberCount() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();
            when(info.getSubscriberCount()).thenReturn(200_000_000L);

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertEquals(200_000_000L, dto.getSubscriberCount());
        }

        @Test
        @DisplayName("Should handle unverified channel")
        void testFrom_UnverifiedChannel() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();
            when(info.isVerified()).thenReturn(false);

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertFalse(dto.isVerified());
        }

        @Test
        @DisplayName("Should handle null description")
        void testFrom_NullDescription() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();
            when(info.getDescription()).thenReturn(null);

            // Act
            ChannelDTO dto = ChannelDTO.from(info);

            // Assert
            assertNull(dto.getDescription());
        }
    }

    @Nested
    @DisplayName("Getters and Setters Tests")
    class GettersSettersTests {

        @Test
        @DisplayName("Should get and set all fields")
        void testGettersSetters() {
            // Arrange
            ChannelDTO dto = new ChannelDTO();
            List<ChannelDTO.ImageDto> images = List.of();
            List<ChannelDTO.TabDto> tabs = List.of();

            // Act
            dto.setServiceId(0);
            dto.setId("UCtest");
            dto.setUrl("https://youtube.com/channel/UCtest");
            dto.setOriginalUrl("https://youtube.com/@test");
            dto.setName("Test Channel");
            dto.setErrors(List.of());
            dto.setSubscriberCount(1000L);
            dto.setDescription("A description");
            dto.setVerified(true);
            dto.setAvatars(images);
            dto.setBanners(images);
            dto.setParentChannelAvatars(images);
            dto.setTabs(tabs);
            dto.setTags(List.of("tag1"));

            // Assert
            assertEquals(0, dto.getServiceId());
            assertEquals("UCtest", dto.getId());
            assertEquals("https://youtube.com/channel/UCtest", dto.getUrl());
            assertEquals("https://youtube.com/@test", dto.getOriginalUrl());
            assertEquals("Test Channel", dto.getName());
            assertTrue(dto.getErrors().isEmpty());
            assertEquals(1000L, dto.getSubscriberCount());
            assertEquals("A description", dto.getDescription());
            assertTrue(dto.isVerified());
            assertSame(images, dto.getAvatars());
            assertSame(images, dto.getBanners());
            assertSame(tabs, dto.getTabs());
            assertEquals(List.of("tag1"), dto.getTags());
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include id, name, subscriberCount, and verified in toString")
        void testToString() {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();

            // Act
            ChannelDTO dto = ChannelDTO.from(info);
            String result = dto.toString();

            // Assert
            assertTrue(result.contains("UCXuqSBlHAE6Xw-yeJA0Tunw"));
            assertTrue(result.contains("Linus Tech Tips"));
            assertTrue(result.contains("15000000"));
            assertTrue(result.contains("true"));
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize ChannelDTO to JSON")
        void testSerialization() throws Exception {
            // Arrange
            ChannelInfo info = buildFullChannelInfo();
            ChannelDTO dto = ChannelDTO.from(info);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"id\""));
            assertTrue(json.contains("\"name\""));
            assertTrue(json.contains("UCXuqSBlHAE6Xw-yeJA0Tunw"));
            assertTrue(json.contains("Linus Tech Tips"));
            assertTrue(json.contains("\"verified\""));
        }
    }
}
