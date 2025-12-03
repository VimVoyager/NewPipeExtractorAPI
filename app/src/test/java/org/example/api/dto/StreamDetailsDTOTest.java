package org.example.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for StreamDetailsDTO.
 * Tests mapping from StreamInfo and JSON serialization.
 */
@DisplayName("StreamDetailsDTO Tests")
class StreamDetailsDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from StreamInfo with all fields")
        void testFrom_AllFields() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Test Video Title");

            Description description = mock(Description.class);
            when(description.getContent()).thenReturn("Test description content");
            when(streamInfo.getDescription()).thenReturn(description);

            Image avatar = mock(Image.class);
            when(avatar.getUrl()).thenReturn("https://yt3.ggpht.com/avatar.jpg");
            when(streamInfo.getUploaderAvatars()).thenReturn(List.of(avatar));

            when(streamInfo.getViewCount()).thenReturn(1000000L);
            when(streamInfo.getLikeCount()).thenReturn(50000L);
            when(streamInfo.getDislikeCount()).thenReturn(500L);
            when(streamInfo.getUploaderName()).thenReturn("Channel Name");
            when(streamInfo.getUploaderSubscriberCount()).thenReturn(500000L);
            when(streamInfo.getTextualUploadDate()).thenReturn("2025-12-01");

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals("Test Video Title", dto.getVideoTitle());
            assertEquals(description, dto.getDescription());
            assertEquals(1, dto.getUploaderAvatars().size());
            assertEquals("https://yt3.ggpht.com/avatar.jpg", dto.getUploaderAvatars().get(0).getUrl());
            assertEquals(1000000L, dto.getViewCount());
            assertEquals(50000L, dto.getLikeCount());
            assertEquals(500L, dto.getDislikeCount());
            assertEquals("Channel Name", dto.getChannelName());
            assertEquals(500000L, dto.getChannelSubscriberCount());
            assertEquals("2025-12-01", dto.getUploadDate());
        }

        @Test
        @DisplayName("Should create DTO from StreamInfo with minimal fields")
        void testFrom_MinimalFields() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Minimal Video");
            when(streamInfo.getDescription()).thenReturn(null);
            when(streamInfo.getUploaderAvatars()).thenReturn(List.of());
            when(streamInfo.getViewCount()).thenReturn(0L);
            when(streamInfo.getLikeCount()).thenReturn(0L);
            when(streamInfo.getDislikeCount()).thenReturn(-1L); // Unknown
            when(streamInfo.getUploaderName()).thenReturn(null);
            when(streamInfo.getUploaderSubscriberCount()).thenReturn(-1L); // Hidden
            when(streamInfo.getTextualUploadDate()).thenReturn(null);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals("Minimal Video", dto.getVideoTitle());
            assertNull(dto.getDescription());
            assertTrue(dto.getUploaderAvatars().isEmpty());
            assertEquals(0L, dto.getViewCount());
            assertEquals(0L, dto.getLikeCount());
            assertEquals(-1L, dto.getDislikeCount());
            assertNull(dto.getChannelName());
            assertEquals(-1L, dto.getChannelSubscriberCount());
            assertNull(dto.getUploadDate());
        }

        @Test
        @DisplayName("Should handle zero counts (new video)")
        void testFrom_ZeroCounts() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("New Video");
            when(streamInfo.getViewCount()).thenReturn(0L);
            when(streamInfo.getLikeCount()).thenReturn(0L);
            when(streamInfo.getDislikeCount()).thenReturn(0L);
            when(streamInfo.getUploaderSubscriberCount()).thenReturn(0L);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals(0L, dto.getViewCount());
            assertEquals(0L, dto.getLikeCount());
            assertEquals(0L, dto.getDislikeCount());
            assertEquals(0L, dto.getChannelSubscriberCount());
        }

        @Test
        @DisplayName("Should handle viral video with very large counts")
        void testFrom_ViralVideo() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Viral Video");
            when(streamInfo.getViewCount()).thenReturn(1_000_000_000L); // 1 billion views
            when(streamInfo.getLikeCount()).thenReturn(50_000_000L); // 50 million likes
            when(streamInfo.getDislikeCount()).thenReturn(1_000_000L); // 1 million dislikes
            when(streamInfo.getUploaderSubscriberCount()).thenReturn(100_000_000L); // 100 million subs

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals(1_000_000_000L, dto.getViewCount());
            assertEquals(50_000_000L, dto.getLikeCount());
            assertEquals(1_000_000L, dto.getDislikeCount());
            assertEquals(100_000_000L, dto.getChannelSubscriberCount());
        }

        @Test
        @DisplayName("Should handle unknown dislike count")
        void testFrom_UnknownDislikes() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video Without Dislikes");
            when(streamInfo.getViewCount()).thenReturn(10000L);
            when(streamInfo.getLikeCount()).thenReturn(500L);
            when(streamInfo.getDislikeCount()).thenReturn(-1L); // YouTube hides dislikes

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals(-1L, dto.getDislikeCount());
        }

        @Test
        @DisplayName("Should handle hidden subscriber count")
        void testFrom_HiddenSubscriberCount() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video From Private Channel");
            when(streamInfo.getUploaderName()).thenReturn("Private Channel");
            when(streamInfo.getUploaderSubscriberCount()).thenReturn(-1L); // Hidden

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals(-1L, dto.getChannelSubscriberCount());
        }

        @Test
        @DisplayName("Should handle multiple uploader avatars")
        void testFrom_MultipleAvatars() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video");

            Image avatar1 = mock(Image.class);
            when(avatar1.getUrl()).thenReturn("https://avatar1.jpg");
            Image avatar2 = mock(Image.class);
            when(avatar2.getUrl()).thenReturn("https://avatar2.jpg");
            Image avatar3 = mock(Image.class);
            when(avatar3.getUrl()).thenReturn("https://avatar3.jpg");

            when(streamInfo.getUploaderAvatars()).thenReturn(List.of(avatar1, avatar2, avatar3));

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals(3, dto.getUploaderAvatars().size());
            assertEquals("https://avatar1.jpg", dto.getUploaderAvatars().get(0).getUrl());
            assertEquals("https://avatar2.jpg", dto.getUploaderAvatars().get(1).getUrl());
            assertEquals("https://avatar3.jpg", dto.getUploaderAvatars().get(2).getUrl());
        }
    }

    @Nested
    @DisplayName("Description Handling Tests")
    class DescriptionHandlingTests {

        @Test
        @DisplayName("Should handle description object")
        void testDescriptionObject() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video");

            Description description = mock(Description.class);
            when(description.getContent()).thenReturn("This is a test description");
            when(streamInfo.getDescription()).thenReturn(description);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertNotNull(dto.getDescription());
            assertEquals("This is a test description", dto.getDescription().getContent());
        }

        @Test
        @DisplayName("Should handle null description")
        void testNullDescription() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video");
            when(streamInfo.getDescription()).thenReturn(null);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertNull(dto.getDescription());
        }

        @Test
        @DisplayName("Should handle empty description")
        void testEmptyDescription() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video");

            Description description = mock(Description.class);
            when(description.getContent()).thenReturn("");
            when(streamInfo.getDescription()).thenReturn(description);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertNotNull(dto.getDescription());
            assertEquals("", dto.getDescription().getContent());
        }

        @Test
        @DisplayName("Should handle very long description")
        void testLongDescription() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video");

            String longContent = "a".repeat(5000);
            Description description = mock(Description.class);
            when(description.getContent()).thenReturn(longContent);
            when(streamInfo.getDescription()).thenReturn(description);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals(5000, dto.getDescription().getContent().length());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize to JSON with all fields")
        void testSerialize_AllFields() throws Exception {
            // Arrange
            StreamDetailsDTO dto = new StreamDetailsDTO();
            dto.setVideoTitle("Test Video");
            dto.setChannelName("Test Channel");
            dto.setViewCount(1000L);
            dto.setLikeCount(100L);
            dto.setDislikeCount(10L);
            dto.setChannelSubscriberCount(5000L);
            dto.setUploadDate("2025-12-01");

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"videoTitle\":\"Test Video\""));
            assertTrue(json.contains("\"channelName\":\"Test Channel\""));
            assertTrue(json.contains("\"viewCount\":1000"));
            assertTrue(json.contains("\"likeCount\":100"));
            assertTrue(json.contains("\"dislikeCount\":10"));
            assertTrue(json.contains("\"channelSubscriberCount\":5000"));
            assertTrue(json.contains("\"uploadDate\":\"2025-12-01\""));
        }

        @Test
        @DisplayName("Should deserialize from JSON")
        void testDeserialize() throws Exception {
            // Arrange
            String json = """
                {
                    "videoTitle": "Deserialized Video",
                    "channelName": "Deserialized Channel",
                    "viewCount": 2000,
                    "likeCount": 200,
                    "dislikeCount": 20,
                    "channelSubscriberCount": 10000,
                    "uploadDate": "2025-12-02"
                }
                """;

            // Act
            StreamDetailsDTO dto = objectMapper.readValue(json, StreamDetailsDTO.class);

            // Assert
            assertEquals("Deserialized Video", dto.getVideoTitle());
            assertEquals("Deserialized Channel", dto.getChannelName());
            assertEquals(2000L, dto.getViewCount());
            assertEquals(200L, dto.getLikeCount());
            assertEquals(20L, dto.getDislikeCount());
            assertEquals(10000L, dto.getChannelSubscriberCount());
            assertEquals("2025-12-02", dto.getUploadDate());
        }

        @Test
        @DisplayName("Should handle round-trip serialization")
        void testRoundTrip() throws Exception {
            // Arrange
            StreamDetailsDTO original = new StreamDetailsDTO();
            original.setVideoTitle("Round Trip Test");
            original.setChannelName("Test Channel");
            original.setViewCount(5000L);
            original.setLikeCount(250L);
            original.setDislikeCount(25L);
            original.setChannelSubscriberCount(15000L);
            original.setUploadDate("2025-12-03");

            // Act
            String json = objectMapper.writeValueAsString(original);
            StreamDetailsDTO deserialized = objectMapper.readValue(json, StreamDetailsDTO.class);

            // Assert
            assertEquals(original.getVideoTitle(), deserialized.getVideoTitle());
            assertEquals(original.getChannelName(), deserialized.getChannelName());
            assertEquals(original.getViewCount(), deserialized.getViewCount());
            assertEquals(original.getLikeCount(), deserialized.getLikeCount());
            assertEquals(original.getDislikeCount(), deserialized.getDislikeCount());
            assertEquals(original.getChannelSubscriberCount(), deserialized.getChannelSubscriberCount());
            assertEquals(original.getUploadDate(), deserialized.getUploadDate());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set all fields")
        void testAllFields() {
            // Arrange
            StreamDetailsDTO dto = new StreamDetailsDTO();
            Description description = mock(Description.class);
            List<Image> avatars = List.of(mock(Image.class));

            // Act
            dto.setVideoTitle("Title");
            dto.setDescription(description);
            dto.setUploaderAvatars(avatars);
            dto.setViewCount(1000L);
            dto.setLikeCount(100L);
            dto.setDislikeCount(10L);
            dto.setChannelName("Channel");
            dto.setChannelSubscriberCount(5000L);
            dto.setUploadDate("2025-12-01");

            // Assert
            assertEquals("Title", dto.getVideoTitle());
            assertEquals(description, dto.getDescription());
            assertEquals(1, dto.getUploaderAvatars().size());
            assertEquals(1000L, dto.getViewCount());
            assertEquals(100L, dto.getLikeCount());
            assertEquals(10L, dto.getDislikeCount());
            assertEquals("Channel", dto.getChannelName());
            assertEquals(5000L, dto.getChannelSubscriberCount());
            assertEquals("2025-12-01", dto.getUploadDate());
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullValues() {
            // Arrange
            StreamDetailsDTO dto = new StreamDetailsDTO();

            // Act
            dto.setVideoTitle(null);
            dto.setDescription(null);
            dto.setUploaderAvatars(null);
            dto.setChannelName(null);
            dto.setUploadDate(null);

            // Assert
            assertNull(dto.getVideoTitle());
            assertNull(dto.getDescription());
            assertNull(dto.getUploaderAvatars());
            assertNull(dto.getChannelName());
            assertNull(dto.getUploadDate());
        }

        @Test
        @DisplayName("Should handle zero values")
        void testZeroValues() {
            // Arrange
            StreamDetailsDTO dto = new StreamDetailsDTO();

            // Act
            dto.setViewCount(0L);
            dto.setLikeCount(0L);
            dto.setDislikeCount(0L);
            dto.setChannelSubscriberCount(0L);

            // Assert
            assertEquals(0L, dto.getViewCount());
            assertEquals(0L, dto.getLikeCount());
            assertEquals(0L, dto.getDislikeCount());
            assertEquals(0L, dto.getChannelSubscriberCount());
        }

        @Test
        @DisplayName("Should handle negative values for unknown counts")
        void testNegativeValues() {
            // Arrange
            StreamDetailsDTO dto = new StreamDetailsDTO();

            // Act
            dto.setDislikeCount(-1L);
            dto.setChannelSubscriberCount(-1L);

            // Assert
            assertEquals(-1L, dto.getDislikeCount());
            assertEquals(-1L, dto.getChannelSubscriberCount());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty video title")
        void testEmptyVideoTitle() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("");

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals("", dto.getVideoTitle());
        }

        @Test
        @DisplayName("Should handle very long video title")
        void testLongVideoTitle() {
            // Arrange
            String longTitle = "A".repeat(500);
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn(longTitle);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals(500, dto.getVideoTitle().length());
            assertEquals(longTitle, dto.getVideoTitle());
        }

        @Test
        @DisplayName("Should handle special characters in title")
        void testSpecialCharactersInTitle() {
            // Arrange
            String specialTitle = "Video: \"Test\" & <Example> 'Quote'";
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn(specialTitle);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals(specialTitle, dto.getVideoTitle());
        }

        @Test
        @DisplayName("Should handle empty uploader avatars list")
        void testEmptyAvatarsList() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video");
            when(streamInfo.getUploaderAvatars()).thenReturn(List.of());

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertTrue(dto.getUploaderAvatars().isEmpty());
        }

        @Test
        @DisplayName("Should handle null uploader avatars")
        void testNullAvatars() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video");
            when(streamInfo.getUploaderAvatars()).thenReturn(null);

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertNull(dto.getUploaderAvatars());
        }

        @Test
        @DisplayName("Should handle special upload date formats")
        void testSpecialUploadDateFormats() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Video");
            when(streamInfo.getTextualUploadDate()).thenReturn("2 days ago");

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert
            assertEquals("2 days ago", dto.getUploadDate());
        }
    }

    @Nested
    @DisplayName("Practical Usage Tests")
    class PracticalUsageTests {

        @Test
        @DisplayName("Should represent complete video metadata")
        void testCompleteVideoMetadata() {
            // Arrange
            StreamInfo streamInfo = mock(StreamInfo.class);
            when(streamInfo.getName()).thenReturn("Complete Video Tutorial");

            Description description = mock(Description.class);
            when(description.getContent()).thenReturn("Full tutorial on testing");
            when(streamInfo.getDescription()).thenReturn(description);

            Image avatar = mock(Image.class);
            when(streamInfo.getUploaderAvatars()).thenReturn(List.of(avatar));

            when(streamInfo.getViewCount()).thenReturn(50000L);
            when(streamInfo.getLikeCount()).thenReturn(2500L);
            when(streamInfo.getDislikeCount()).thenReturn(50L);
            when(streamInfo.getUploaderName()).thenReturn("Tutorial Channel");
            when(streamInfo.getUploaderSubscriberCount()).thenReturn(100000L);
            when(streamInfo.getTextualUploadDate()).thenReturn("1 week ago");

            // Act
            StreamDetailsDTO dto = StreamDetailsDTO.from(streamInfo);

            // Assert - All metadata properly captured
            assertNotNull(dto.getVideoTitle());
            assertNotNull(dto.getDescription());
            assertFalse(dto.getUploaderAvatars().isEmpty());
            assertTrue(dto.getViewCount() > 0);
            assertTrue(dto.getLikeCount() > 0);
            assertTrue(dto.getChannelSubscriberCount() > 0);
            assertNotNull(dto.getUploadDate());
        }
    }
}