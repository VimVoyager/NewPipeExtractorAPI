package org.example.api.dto.playlist;

import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.List;

public class PlaylistDTO {

    private String id;
    private String url;
    private String originalUrl;
    private String name;
    private String uploaderUrl;
    private String uploaderName;
    private String subChannelUrl;
    private String subChannelName;
    private DescriptionDto description;
    private List<ThumbnailDto> thumbnails;
    private List<ThumbnailDto> banners;
    private List<AvatarDto> uploaderAvatars;
    private List<RelatedItemDto> relatedItems;
    private long streamCount;

    // ── Nested Records ───────────────────────────────────────────────────────────

    public record DescriptionDto(String content, int type) {}
    public record ThumbnailDto(String url, int height, int width) {}
    public record AvatarDto(String url, int height, int width) {}
    public record RelatedItemDto(
            String infoType,
            String url,
            String name,
            String uploaderName,
            String uploaderUrl,
            String textualUploadDate,
            long viewCount,
            long duration,
            boolean uploaderVerified,
            boolean shortFormContent,
            List<ThumbnailDto> thumbnails,
            List<AvatarDto> uploaderAvatars
    ) {}

    // ── Constructors ─────────────────────────────────────────────────────────────

    public PlaylistDTO() {}

    // ── Static Factory ───────────────────────────────────────────────────────────

    public static PlaylistDTO from(PlaylistInfo info) {
        PlaylistDTO dto = new PlaylistDTO();

        dto.setId(info.getId());
        dto.setUrl(info.getUrl());
        dto.setOriginalUrl(info.getOriginalUrl());
        dto.setName(info.getName());
        dto.setUploaderUrl(info.getUploaderUrl());
        dto.setUploaderName(info.getUploaderName());
        dto.setSubChannelUrl(info.getSubChannelUrl());
        dto.setSubChannelName(info.getSubChannelName());
        dto.setDescription(info.getDescription() != null
                ? new DescriptionDto(
                info.getDescription().getContent(),
                info.getDescription().getType())
                : null);
        dto.setThumbnails(info.getThumbnails().stream()
                .map(t -> new ThumbnailDto(t.getUrl(), t.getHeight(), t.getWidth()))
                .toList());
        dto.setBanners(info.getBanners().stream()
                .map(b -> new ThumbnailDto(b.getUrl(), b.getHeight(), b.getWidth()))
                .toList());
        dto.setUploaderAvatars(info.getUploaderAvatars().stream()
                .map(a -> new AvatarDto(a.getUrl(), a.getHeight(), a.getWidth()))
                .toList());
        dto.setRelatedItems(info.getRelatedItems().stream()
                .filter(item -> item instanceof StreamInfoItem)
                .map(item -> {
                    StreamInfoItem s = (StreamInfoItem) item;
                    return new RelatedItemDto(
                            "STREAM",
                            s.getUrl(),
                            s.getName(),
                            s.getUploaderName(),
                            s.getUploaderUrl(),
                            s.getTextualUploadDate(),
                            s.getViewCount(),
                            s.getDuration(),
                            s.isUploaderVerified(),
                            s.isShortFormContent(),
                            s.getThumbnails().stream()
                                    .map(t -> new ThumbnailDto(t.getUrl(), t.getHeight(), t.getWidth()))
                                    .toList(),
                            List.of()
                    );
                })
                .toList());
        dto.setStreamCount(info.getStreamCount());

        return dto;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUploaderUrl() { return uploaderUrl; }
    public void setUploaderUrl(String uploaderUrl) { this.uploaderUrl = uploaderUrl; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public String getSubChannelUrl() { return subChannelUrl; }
    public void setSubChannelUrl(String subChannelUrl) { this.subChannelUrl = subChannelUrl; }

    public String getSubChannelName() { return subChannelName; }
    public void setSubChannelName(String subChannelName) { this.subChannelName = subChannelName; }

    public DescriptionDto getDescription() { return description; }
    public void setDescription(DescriptionDto description) { this.description = description; }

    public List<ThumbnailDto> getThumbnails() { return thumbnails; }
    public void setThumbnails(List<ThumbnailDto> thumbnails) { this.thumbnails = thumbnails; }

    public List<ThumbnailDto> getBanners() { return banners; }
    public void setBanners(List<ThumbnailDto> banners) { this.banners = banners; }

    public List<AvatarDto> getUploaderAvatars() { return uploaderAvatars; }
    public void setUploaderAvatars(List<AvatarDto> uploaderAvatars) { this.uploaderAvatars = uploaderAvatars; }

    public List<RelatedItemDto> getRelatedItems() { return relatedItems; }
    public void setRelatedItems(List<RelatedItemDto> relatedItems) { this.relatedItems = relatedItems; }

    public long getStreamCount() { return streamCount; }
    public void setStreamCount(long streamCount) { this.streamCount = streamCount; }

    @Override
    public String toString() {
        return "PlaylistDTO{id='%s', name='%s', streamCount=%d}".formatted(id, name, streamCount);
    }
}