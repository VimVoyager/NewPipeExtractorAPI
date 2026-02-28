package org.example.api.dto;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

/**
 * Data Transfer Object representing paginated content from a channel tab
 * (Videos, Shorts, Livestreams, or Playlists).
 *
 * <p>Mirrors the structure of {@link ChannelTabInfo} and its paged items,
 * keeping raw NewPipe types out of the API surface.</p>
 */
public class ChannelTabDTO {

    // ── Nested Records ──────────────────────────────────────────────────────────

    /**
     * Thumbnail image for a video item.
     */
    public record ThumbnailDto(String url, int height, int width) {}

    /**
     * A single video/short/livestream item within the tab.
     * Maps to {@link StreamInfoItem}.
     */
    public record VideoItemDto(
            String url,
            String name,
            String uploaderName,
            String uploaderUrl,
            boolean uploaderVerified,
            long duration,          // seconds; -1 if unknown (e.g. live streams)
            long viewCount,
            String textualUploadDate,
            boolean isShortFormContent,
            List<ThumbnailDto> thumbnails
    ) {
        /** Build from a NewPipe {@link StreamInfoItem}. */
        public static VideoItemDto from(StreamInfoItem item) {
            List<ThumbnailDto> thumbnails = item.getThumbnails().stream()
                    .map(img -> new ThumbnailDto(img.getUrl(), img.getHeight(), img.getWidth()))
                    .toList();

            return new VideoItemDto(
                    item.getUrl(),
                    item.getName(),
                    item.getUploaderName(),
                    item.getUploaderUrl(),
                    item.isUploaderVerified(),
                    item.getDuration(),
                    item.getViewCount(),
                    item.getTextualUploadDate(),
                    item.isShortFormContent(),
                    thumbnails
            );
        }
    }

    /**
     * Pagination cursor — pass {@code nextPage} back to the
     * {@code /channels/{id}/tab/page} endpoint to load more items.
     * Will be {@code null} when there are no more pages.
     */
    public record PageDto(String nextPage) {}

    // ── Main DTO ────────────────────────────────────────────────────────────────

    /** The tab type that was queried (e.g. "videos", "shorts", "livestreams"). */
    private String tab;

    /** Channel ID this tab belongs to. */
    private String channelId;

    /** Items on the current page. Only {@link StreamInfoItem}s are mapped;
     *  other item types (playlists, channels) are silently skipped for now. */
    private List<VideoItemDto> items;

    /** Pagination info. Null when no further pages exist. */
    private PageDto nextPage;

    // ── Constructors ────────────────────────────────────────────────────────────

    public ChannelTabDTO() {}

    // ── Static Factories ────────────────────────────────────────────────────────

    /**
     * Builds a {@code ChannelTabDTO} from the initial page returned by
     * {@link ChannelTabInfo#getInfo(org.schabi.newpipe.extractor.linkhandler.ListLinkHandler)}.
     *
     * @param tabInfo   the resolved tab info
     * @param tab       the tab type string (e.g. {@code ChannelTabs.VIDEOS})
     * @param channelId the channel ID
     */
    public static ChannelTabDTO from(ChannelTabInfo tabInfo, String tab, String channelId) {
        ChannelTabDTO dto = new ChannelTabDTO();
        dto.tab = tab;
        dto.channelId = channelId;
        dto.items = mapItems(tabInfo.getRelatedItems());
        dto.nextPage = buildNextPage(tabInfo.getNextPage());
        return dto;
    }

    /**
     * Builds a {@code ChannelTabDTO} from a subsequent page returned by
     * {@link ChannelTabInfo#getMoreItems}.
     *
     * @param page      the paged result
     * @param tab       the tab type string
     * @param channelId the channel ID
     */
    public static ChannelTabDTO fromPage(InfoItemsPage<InfoItem> page, String tab, String channelId) {
        ChannelTabDTO dto = new ChannelTabDTO();
        dto.tab = tab;
        dto.channelId = channelId;
        dto.items = mapItems(page.getItems());
        dto.nextPage = buildNextPage(page.getNextPage());
        return dto;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private static List<VideoItemDto> mapItems(List<? extends InfoItem> rawItems) {
        return rawItems.stream()
                .filter(item -> item instanceof StreamInfoItem)
                .map(item -> VideoItemDto.from((StreamInfoItem) item))
                .toList();
    }

    private static PageDto buildNextPage(org.schabi.newpipe.extractor.Page nextPage) {
        if (nextPage == null || nextPage.getUrl() == null) return null;
        return new PageDto(nextPage.getUrl());
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public String getTab() { return tab; }
    public void setTab(String tab) { this.tab = tab; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public List<VideoItemDto> getItems() { return items; }
    public void setItems(List<VideoItemDto> items) { this.items = items; }

    public PageDto getNextPage() { return nextPage; }
    public void setNextPage(PageDto nextPage) { this.nextPage = nextPage; }
}