package org.example.api.dto.kiosk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for KioskDTO.
 */
@DisplayName("KioskDTO Tests")
class KioskDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // ── KioskDTO.from(KioskInfo) ────────────────────────────────────────

    @Nested
    @DisplayName("KioskDTO mapping")
    class KioskDtoMappingTests {

        @Test
        @DisplayName("Maps a KioskInfo end-to-end, including nested items and next page")
        void mapsKioskInfoEndToEnd() {
            Image thumbImage = mock(Image.class);
            when(thumbImage.getUrl()).thenReturn("https://i.ytimg.com/vi/x/thumb.jpg");
            when(thumbImage.getWidth()).thenReturn(320);
            when(thumbImage.getHeight()).thenReturn(180);

            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getName()).thenReturn("Trending Video");
            when(streamItem.getUrl()).thenReturn("https://youtube.com/watch?v=x");
            when(streamItem.getUploaderName()).thenReturn("Some Channel");
            when(streamItem.getUploaderUrl()).thenReturn("https://youtube.com/channel/x");
            when(streamItem.isUploaderVerified()).thenReturn(true);
            when(streamItem.getDuration()).thenReturn(120L);
            when(streamItem.getViewCount()).thenReturn(500L);
            when(streamItem.getUploadDate()).thenReturn(null);
            when(streamItem.getTextualUploadDate()).thenReturn("2 days ago");
            when(streamItem.getStreamType()).thenReturn(StreamType.VIDEO_STREAM);
            when(streamItem.getThumbnails()).thenReturn(List.of(thumbImage));

            Page nextPage = new Page(
                    "https://youtube.com/next", "pageId", List.of("id1"), null, "token".getBytes());

            KioskInfo kioskInfo = mock(KioskInfo.class);
            when(kioskInfo.getServiceId()).thenReturn(0);
            when(kioskInfo.getId()).thenReturn("trending");
            when(kioskInfo.getName()).thenReturn("Trending");
            when(kioskInfo.getUrl()).thenReturn("https://youtube.com/feed/trending");
            when(kioskInfo.getRelatedItems()).thenReturn(List.of(streamItem));
            when(kioskInfo.getNextPage()).thenReturn(nextPage);

            KioskDTO expected = new KioskDTO(
                    0,
                    "trending",
                    "Trending",
                    "https://youtube.com/feed/trending",
                    List.of(new KioskDTO.KioskStreamItemDTO(
                            "Trending Video",
                            "https://youtube.com/watch?v=x",
                            "Some Channel",
                            "https://youtube.com/channel/x",
                            true,
                            120L,
                            500L,
                            null,
                            "2 days ago",
                            "VIDEO_STREAM",
                            List.of(new KioskDTO.ThumbnailDTO(
                                    "https://i.ytimg.com/vi/x/thumb.jpg", 320, 180))
                    )),
                    new KioskDTO.NextPageDTO(
                            "https://youtube.com/next",
                            "pageId",
                            List.of("id1"),
                            Base64.getEncoder().encodeToString("token".getBytes())
                    )
            );

            assertThat(KioskDTO.from(kioskInfo)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Leaves nextPage null when the kiosk has no next page")
        void leavesNextPageNullWhenAbsent() {
            KioskInfo kioskInfo = mock(KioskInfo.class);
            when(kioskInfo.getServiceId()).thenReturn(0);
            when(kioskInfo.getId()).thenReturn("trending");
            when(kioskInfo.getName()).thenReturn("Trending");
            when(kioskInfo.getUrl()).thenReturn("https://youtube.com/feed/trending");
            when(kioskInfo.getRelatedItems()).thenReturn(List.of());
            when(kioskInfo.getNextPage()).thenReturn(null);

            assertThat(KioskDTO.from(kioskInfo).nextPage()).isNull();
        }
    }

    // ── KioskStreamItemDTO.from(StreamInfoItem) ─────────────────────────

    @Nested
    @DisplayName("KioskStreamItemDTO mapping")
    class StreamItemMappingTests {

        @Test
        @DisplayName("Leaves uploadDate null when the stream has none")
        void leavesUploadDateNullWhenAbsent() {
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getThumbnails()).thenReturn(List.of());
            when(streamItem.getUploadDate()).thenReturn(null);
            when(streamItem.getStreamType()).thenReturn(StreamType.VIDEO_STREAM);

            assertThat(KioskDTO.KioskStreamItemDTO.from(streamItem).uploadDate()).isNull();
        }

        @Test
        @DisplayName("Currently throws NPE when streamType is null")
        void throwsWhenStreamTypeIsNull() {
            StreamInfoItem streamItem = mock(StreamInfoItem.class);
            when(streamItem.getThumbnails()).thenReturn(List.of());
            when(streamItem.getStreamType()).thenReturn(null);

            assertThatThrownBy(() -> KioskDTO.KioskStreamItemDTO.from(streamItem))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ── ThumbnailDTO.from(Image) ────────────────────────────────────────

    @Nested
    @DisplayName("ThumbnailDTO mapping")
    class ThumbnailMappingTests {

        @Test
        @DisplayName("Maps url, width and height from an Image")
        void mapsFromImage() {
            Image image = mock(Image.class);
            when(image.getUrl()).thenReturn("https://i.ytimg.com/vi/x/thumb.jpg");
            when(image.getWidth()).thenReturn(320);
            when(image.getHeight()).thenReturn(180);

            assertThat(KioskDTO.ThumbnailDTO.from(image))
                    .isEqualTo(new KioskDTO.ThumbnailDTO(
                            "https://i.ytimg.com/vi/x/thumb.jpg", 320, 180));
        }
    }

    // ── NextPageDTO.from(Page) / toPage(...) ────────────────────────────

    @Nested
    @DisplayName("NextPageDTO mapping")
    class NextPageMappingTests {

        @Test
        @DisplayName("Returns null when the extractor page is null")
        void fromReturnsNullForNullPage() {
            assertThat(KioskDTO.NextPageDTO.from(null)).isNull();
        }

        @Test
        @DisplayName("Base64-encodes the body and leaves other fields untouched")
        void fromEncodesBody() {
            Page page = new Page(
                    "https://youtube.com/next", "pageId", List.of("id1"), null, "token".getBytes());

            assertThat(KioskDTO.NextPageDTO.from(page)).isEqualTo(new KioskDTO.NextPageDTO(
                    "https://youtube.com/next",
                    "pageId",
                    List.of("id1"),
                    Base64.getEncoder().encodeToString("token".getBytes())));
        }

        @Test
        @DisplayName("Leaves body null when the page body is null")
        void fromLeavesNullBodyNull() {
            Page page = new Page("https://youtube.com/next", "pageId", List.of("id1"), null, null);

            assertThat(KioskDTO.NextPageDTO.from(page).body()).isNull();
        }
    }

    // ── JSON ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("JSON serialization")
    class JsonSerializationTests {

        @Test
        @DisplayName("Serializes record accessor names as JSON property names")
        void serializesUsingAccessorNames() throws Exception {
            KioskDTO dto = new KioskDTO(
                    0, "trending", "Trending", "https://youtube.com/feed/trending", List.of(), null);

            String json = objectMapper.writeValueAsString(dto);

            assertThat(json).contains(
                    "\"serviceId\":0",
                    "\"id\":\"trending\"",
                    "\"name\":\"Trending\"",
                    "\"url\":\"https://youtube.com/feed/trending\"");
        }
    }
}