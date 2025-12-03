package org.example.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.localization.DateWrapper;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for SearchItemDTO.
 * Tests mapping from different InfoItem types and JSON serialization.
 */
@DisplayName("SearchItemDTO Tests")
class SearchItemDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Stream Mapping Tests")
    class StreamMappingTests {

        @Test
        @DisplayName("Should map StreamInfoItem with all fields")
        void testMapStreamInfo_AllFields() {
            // Arrange
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("Test Video");
            when(streamItem.getUrl()).thenReturn("https://youtube.com/watch?v=test123");

            Image thumbnail = mock(Image.class);
            when(thumbnail.getUrl()).thenReturn("https://i.ytimg.com/vi/test123/maxresdefault.jpg");
            when(streamItem.getThumbnails()).thenReturn(List.of(thumbnail));

            when(streamItem.getUploaderName()).thenReturn("Test Channel");
            when(streamItem.getUploaderUrl()).thenReturn("https://youtube.com/channel/test");
            when(streamItem.isUploaderVerified()).thenReturn(true);

            Image avatar = mock(Image.class);
            when(avatar.getUrl()).thenReturn("https://yt3.ggpht.com/avatar.jpg");
            when(streamItem.getUploaderAvatars()).thenReturn(List.of(avatar));

            when(streamItem.getDuration()).thenReturn(300L);
            when(streamItem.getViewCount()).thenReturn(1000000L);

            DateWrapper uploadDate = mock(DateWrapper.class);
            OffsetDateTime dateTime = OffsetDateTime.parse("2025-12-01T10:00:11Z");
            when(uploadDate.offsetDateTime()).thenReturn(dateTime);
            when(streamItem.getUploadDate()).thenReturn(uploadDate);

            when(streamItem.getStreamType()).thenReturn(StreamType.VIDEO_STREAM);
            when(streamItem.isShortFormContent()).thenReturn(false);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(streamItem);

            // Assert
            assertEquals("stream", dto.getType());
            assertEquals("Test Video", dto.getName());
            assertEquals("https://youtube.com/watch?v=test123", dto.getUrl());
            assertEquals("https://i.ytimg.com/vi/test123/maxresdefault.jpg", dto.getThumbnailUrl());
            assertEquals("Test Channel", dto.getUploaderName());
            assertEquals("https://youtube.com/channel/test", dto.getUploaderUrl());
            assertTrue(dto.getUploaderVerified());
            assertEquals("https://yt3.ggpht.com/avatar.jpg", dto.getUploaderAvatarUrl());
            assertEquals(300L, dto.getDuration());
            assertEquals(1000000L, dto.getViewCount());
            assertEquals("2025-12-01T10:00:11Z", dto.getUploadDate());
            assertEquals("VIDEO_STREAM", dto.getStreamType());
            assertFalse(dto.getShortFormContent());
        }

        @Test
        @DisplayName("Should map StreamInfoItem with minimal fields")
        void testMapStreamInfo_MinimalFields() {
            // Arrange
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("Minimal Video");
            when(streamItem.getUrl()).thenReturn("https://youtube.com/watch?v=min123");
            when(streamItem.getThumbnails()).thenReturn(List.of());
            when(streamItem.getUploaderAvatars()).thenReturn(List.of());
            when(streamItem.getUploadDate()).thenReturn(null);
            when(streamItem.getStreamType()).thenReturn(null);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(streamItem);

            // Assert
            assertEquals("stream", dto.getType());
            assertEquals("Minimal Video", dto.getName());
            assertEquals("https://youtube.com/watch?v=min123", dto.getUrl());
            assertNull(dto.getThumbnailUrl());
            assertNull(dto.getUploaderAvatarUrl());
            assertNull(dto.getUploadDate());
            assertNull(dto.getStreamType());
        }

        @Test
        @DisplayName("Should map live stream correctly")
        void testMapLiveStream() {
            // Arrange
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("Live Stream");
            when(streamItem.getUrl()).thenReturn("https://youtube.com/watch?v=live123");
            when(streamItem.getThumbnails()).thenReturn(List.of());
            when(streamItem.getStreamType()).thenReturn(StreamType.LIVE_STREAM);
            when(streamItem.getDuration()).thenReturn(-1L); // Live streams have -1 duration

            // Act
            SearchItemDTO dto = SearchItemDTO.from(streamItem);

            // Assert
            assertEquals("LIVE_STREAM", dto.getStreamType());
            assertEquals(-1L, dto.getDuration());
        }

        @Test
        @DisplayName("Should map short form content (YouTube Shorts)")
        void testMapShortFormContent() {
            // Arrange
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("YouTube Short");
            when(streamItem.getUrl()).thenReturn("https://youtube.com/shorts/short123");
            when(streamItem.getThumbnails()).thenReturn(List.of());
            when(streamItem.isShortFormContent()).thenReturn(true);
            when(streamItem.getDuration()).thenReturn(30L); // Shorts are usually <60s

            // Act
            SearchItemDTO dto = SearchItemDTO.from(streamItem);

            // Assert
            assertTrue(dto.getShortFormContent());
            assertTrue(dto.getDuration() < 60);
        }

        @Test
        @DisplayName("Should handle multiple thumbnails and use first")
        void testMapStreamInfo_MultipleThumbnails() {
            // Arrange
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("Test Video");
            when(streamItem.getUrl()).thenReturn("https://youtube.com/watch?v=test");

            Image thumb1 = mock(Image.class);
            when(thumb1.getUrl()).thenReturn("https://thumb1.jpg");
            Image thumb2 = mock(Image.class);
            when(thumb2.getUrl()).thenReturn("https://thumb2.jpg");
            when(streamItem.getThumbnails()).thenReturn(List.of(thumb1, thumb2));

            // Act
            SearchItemDTO dto = SearchItemDTO.from(streamItem);

            // Assert
            assertEquals("https://thumb1.jpg", dto.getThumbnailUrl());
        }
    }

    @Nested
    @DisplayName("Channel Mapping Tests")
    class ChannelMappingTests {

        @Test
        @DisplayName("Should map ChannelInfoItem with all fields")
        void testMapChannelInfo_AllFields() {
            // Arrange
            ChannelInfoItem channelItem = mock(ChannelInfoItem.class);
            when(channelItem.getName()).thenReturn("Test Channel");
            when(channelItem.getUrl()).thenReturn("https://youtube.com/channel/test");

            Image thumbnail = mock(Image.class);
            when(thumbnail.getUrl()).thenReturn("https://yt3.ggpht.com/channel.jpg");
            when(channelItem.getThumbnails()).thenReturn(List.of(thumbnail));

            when(channelItem.getSubscriberCount()).thenReturn(1000000L);
            when(channelItem.getStreamCount()).thenReturn(500L);
            when(channelItem.getDescription()).thenReturn("Channel description");
            when(channelItem.isVerified()).thenReturn(true);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(channelItem);

            // Assert
            assertEquals("channel", dto.getType());
            assertEquals("Test Channel", dto.getName());
            assertEquals("https://youtube.com/channel/test", dto.getUrl());
            assertEquals("https://yt3.ggpht.com/channel.jpg", dto.getThumbnailUrl());
            assertEquals(1000000L, dto.getSubscriberCount());
            assertEquals(500L, dto.getStreamCount());
            assertEquals("Channel description", dto.getDescription());
            assertTrue(dto.getUploaderVerified());
        }

        @Test
        @DisplayName("Should map ChannelInfoItem with minimal fields")
        void testMapChannelInfo_MinimalFields() {
            // Arrange
            ChannelInfoItem channelItem = mock(ChannelInfoItem.class);
            when(channelItem.getName()).thenReturn("Minimal Channel");
            when(channelItem.getUrl()).thenReturn("https://youtube.com/channel/minimal");
            when(channelItem.getThumbnails()).thenReturn(List.of());
            when(channelItem.getSubscriberCount()).thenReturn(-1L); // Unknown
            when(channelItem.getStreamCount()).thenReturn(-1L);
            when(channelItem.getDescription()).thenReturn(null);
            when(channelItem.isVerified()).thenReturn(false);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(channelItem);

            // Assert
            assertEquals("channel", dto.getType());
            assertEquals("Minimal Channel", dto.getName());
            assertEquals(-1L, dto.getSubscriberCount());
            assertEquals(-1L, dto.getStreamCount());
            assertNull(dto.getDescription());
            assertFalse(dto.getUploaderVerified());
        }

        @Test
        @DisplayName("Should handle channel with no videos")
        void testMapChannelInfo_NoVideos() {
            // Arrange
            ChannelInfoItem channelItem = mock(ChannelInfoItem.class);
            when(channelItem.getName()).thenReturn("New Channel");
            when(channelItem.getUrl()).thenReturn("https://youtube.com/channel/new");
            when(channelItem.getThumbnails()).thenReturn(List.of());
            when(channelItem.getStreamCount()).thenReturn(0L);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(channelItem);

            // Assert
            assertEquals(0L, dto.getStreamCount());
        }
    }

    @Nested
    @DisplayName("Playlist Mapping Tests")
    class PlaylistMappingTests {

        @Test
        @DisplayName("Should map PlaylistInfoItem with all fields")
        void testMapPlaylistInfo_AllFields() {
            // Arrange
            PlaylistInfoItem playlistItem = mock(PlaylistInfoItem.class);
            when(playlistItem.getName()).thenReturn("Test Playlist");
            when(playlistItem.getUrl()).thenReturn("https://youtube.com/playlist?list=test");

            Image thumbnail = mock(Image.class);
            when(thumbnail.getUrl()).thenReturn("https://i.ytimg.com/playlist.jpg");
            when(playlistItem.getThumbnails()).thenReturn(List.of(thumbnail));

            when(playlistItem.getUploaderName()).thenReturn("Playlist Creator");
            when(playlistItem.getUploaderUrl()).thenReturn("https://youtube.com/channel/creator");
            when(playlistItem.getStreamCount()).thenReturn(50L);
            when(playlistItem.getPlaylistType()).thenReturn(org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType.NORMAL);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(playlistItem);

            // Assert
            assertEquals("playlist", dto.getType());
            assertEquals("Test Playlist", dto.getName());
            assertEquals("https://youtube.com/playlist?list=test", dto.getUrl());
            assertEquals("https://i.ytimg.com/playlist.jpg", dto.getThumbnailUrl());
            assertEquals("Playlist Creator", dto.getUploaderName());
            assertEquals("https://youtube.com/channel/creator", dto.getUploaderUrl());
            assertEquals(50L, dto.getVideoCount());
            assertEquals("NORMAL", dto.getPlaylistType());
        }

        @Test
        @DisplayName("Should map PlaylistInfoItem with minimal fields")
        void testMapPlaylistInfo_MinimalFields() {
            // Arrange
            PlaylistInfoItem playlistItem = mock(PlaylistInfoItem.class);
            when(playlistItem.getName()).thenReturn("Minimal Playlist");
            when(playlistItem.getUrl()).thenReturn("https://youtube.com/playlist?list=min");
            when(playlistItem.getThumbnails()).thenReturn(List.of());
            when(playlistItem.getUploaderName()).thenReturn(null);
            when(playlistItem.getUploaderUrl()).thenReturn(null);
            when(playlistItem.getStreamCount()).thenReturn(-1L);
            when(playlistItem.getPlaylistType()).thenReturn(null);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(playlistItem);

            // Assert
            assertEquals("playlist", dto.getType());
            assertEquals("Minimal Playlist", dto.getName());
            assertNull(dto.getUploaderName());
            assertEquals(-1L, dto.getVideoCount());
            assertNull(dto.getPlaylistType());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize stream item to JSON")
        void testSerializeStreamItem() throws Exception {
            // Arrange
            SearchItemDTO dto = new SearchItemDTO();
            dto.setType("stream");
            dto.setName("Test Video");
            dto.setUrl("https://youtube.com/watch?v=test");
            dto.setDuration(300L);
            dto.setViewCount(1000000L);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"type\":\"stream\""));
            assertTrue(json.contains("\"name\":\"Test Video\""));
            assertTrue(json.contains("\"duration\":300"));
            assertTrue(json.contains("\"viewCount\":1000000"));
        }

        @Test
        @DisplayName("Should deserialize stream item from JSON")
        void testDeserializeStreamItem() throws Exception {
            // Arrange
            String json = """
                {
                    "type": "stream",
                    "name": "Test Video",
                    "url": "https://youtube.com/watch?v=test",
                    "duration": 300,
                    "viewCount": 1000000
                }
                """;

            // Act
            SearchItemDTO dto = objectMapper.readValue(json, SearchItemDTO.class);

            // Assert
            assertEquals("stream", dto.getType());
            assertEquals("Test Video", dto.getName());
            assertEquals(300L, dto.getDuration());
            assertEquals(1000000L, dto.getViewCount());
        }

        @Test
        @DisplayName("Should serialize channel item to JSON")
        void testSerializeChannelItem() throws Exception {
            // Arrange
            SearchItemDTO dto = new SearchItemDTO();
            dto.setType("channel");
            dto.setName("Test Channel");
            dto.setUrl("https://youtube.com/channel/test");
            dto.setSubscriberCount(1000000L);
            dto.setStreamCount(500L);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"type\":\"channel\""));
            assertTrue(json.contains("\"subscriberCount\":1000000"));
            assertTrue(json.contains("\"streamCount\":500"));
        }

        @Test
        @DisplayName("Should serialize playlist item to JSON")
        void testSerializePlaylistItem() throws Exception {
            // Arrange
            SearchItemDTO dto = new SearchItemDTO();
            dto.setType("playlist");
            dto.setName("Test Playlist");
            dto.setUrl("https://youtube.com/playlist?list=test");
            dto.setVideoCount(50L);

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"type\":\"playlist\""));
            assertTrue(json.contains("\"videoCount\":50"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null thumbnails list")
        void testNullThumbnails() {
            // Arrange
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("Test");
            when(streamItem.getUrl()).thenReturn("https://test.com");
            when(streamItem.getThumbnails()).thenReturn(null);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(streamItem);

            // Assert
            assertNull(dto.getThumbnailUrl());
        }

        @Test
        @DisplayName("Should handle empty thumbnails list")
        void testEmptyThumbnails() {
            // Arrange
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("Test");
            when(streamItem.getUrl()).thenReturn("https://test.com");
            when(streamItem.getThumbnails()).thenReturn(List.of());

            // Act
            SearchItemDTO dto = SearchItemDTO.from(streamItem);

            // Assert
            assertNull(dto.getThumbnailUrl());
        }

        @Test
        @DisplayName("Should handle very large view counts")
        void testLargeViewCount() {
            // Arrange
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("Viral Video");
            when(streamItem.getUrl()).thenReturn("https://youtube.com/watch?v=viral");
            when(streamItem.getThumbnails()).thenReturn(List.of());
            when(streamItem.getViewCount()).thenReturn(5_000_000_000L); // 5 billion views

            // Act
            SearchItemDTO dto = SearchItemDTO.from(streamItem);

            // Assert
            assertEquals(5_000_000_000L, dto.getViewCount());
        }

        @Test
        @DisplayName("Should handle unknown subscriber count (-1)")
        void testUnknownSubscriberCount() {
            // Arrange
            ChannelInfoItem channelItem = mock(ChannelInfoItem.class);
            when(channelItem.getName()).thenReturn("Hidden Subs Channel");
            when(channelItem.getUrl()).thenReturn("https://youtube.com/channel/hidden");
            when(channelItem.getThumbnails()).thenReturn(List.of());
            when(channelItem.getSubscriberCount()).thenReturn(-1L);

            // Act
            SearchItemDTO dto = SearchItemDTO.from(channelItem);

            // Assert
            assertEquals(-1L, dto.getSubscriberCount());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set all stream fields")
        void testStreamFields() {
            // Arrange
            SearchItemDTO dto = new SearchItemDTO();

            // Act
            dto.setType("stream");
            dto.setName("Video Name");
            dto.setUrl("https://youtube.com/watch?v=test");
            dto.setThumbnailUrl("https://thumb.jpg");
            dto.setUploaderName("Channel");
            dto.setUploaderUrl("https://channel.url");
            dto.setUploaderAvatarUrl("https://avatar.jpg");
            dto.setUploaderVerified(true);
            dto.setDuration(300L);
            dto.setViewCount(1000L);
            dto.setUploadDate("2025-12-01");
            dto.setStreamType("VIDEO_STREAM");
            dto.setShortFormContent(false);

            // Assert
            assertEquals("stream", dto.getType());
            assertEquals("Video Name", dto.getName());
            assertEquals("https://youtube.com/watch?v=test", dto.getUrl());
            assertEquals("https://thumb.jpg", dto.getThumbnailUrl());
            assertEquals("Channel", dto.getUploaderName());
            assertEquals("https://channel.url", dto.getUploaderUrl());
            assertEquals("https://avatar.jpg", dto.getUploaderAvatarUrl());
            assertTrue(dto.getUploaderVerified());
            assertEquals(300L, dto.getDuration());
            assertEquals(1000L, dto.getViewCount());
            assertEquals("2025-12-01", dto.getUploadDate());
            assertEquals("VIDEO_STREAM", dto.getStreamType());
            assertFalse(dto.getShortFormContent());
        }

        @Test
        @DisplayName("Should get and set all channel fields")
        void testChannelFields() {
            // Arrange
            SearchItemDTO dto = new SearchItemDTO();

            // Act
            dto.setType("channel");
            dto.setSubscriberCount(1000000L);
            dto.setStreamCount(500L);
            dto.setDescription("Channel description");

            // Assert
            assertEquals("channel", dto.getType());
            assertEquals(1000000L, dto.getSubscriberCount());
            assertEquals(500L, dto.getStreamCount());
            assertEquals("Channel description", dto.getDescription());
        }

        @Test
        @DisplayName("Should get and set all playlist fields")
        void testPlaylistFields() {
            // Arrange
            SearchItemDTO dto = new SearchItemDTO();

            // Act
            dto.setType("playlist");
            dto.setPlaylistType("NORMAL");
            dto.setVideoCount(50L);

            // Assert
            assertEquals("playlist", dto.getType());
            assertEquals("NORMAL", dto.getPlaylistType());
            assertEquals(50L, dto.getVideoCount());
        }
    }
}