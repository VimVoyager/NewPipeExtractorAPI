package org.example.api.dto;

import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import java.util.List;

/**
 * Data Transfer Object representing a YouTube channel extracted via NewPipe.
 * Maps to the JSON structure returned by {@link ChannelInfo}.
 */
public class ChannelDTO {

    // ── Nested Records ──────────────────────────────────────────────────────────

    /**
     * An avatar or banner image associated with the channel.
     */
    public record ImageDto(
            String url,
            int height,
            int width,
            String estimatedResolutionLevel
    ) {}

    /**
     * A navigable tab on the channel page (Videos, Shorts, Live, Playlists, etc.).
     */
    public record TabDto(
            String originalUrl,
            String url,
            String id,
            List<String> contentFilters,
            String sortFilter,
            String baseUrl
    ) {}

    // ── Main DTO ────────────────────────────────────────────────────────────────

    private int serviceId;
    private String id;
    private String url;
    private String originalUrl;
    private String name;
    private List<Object> errors;

    private String parentChannelName;
    private String parentChannelUrl;
    private String feedUrl;

    private long subscriberCount;
    private String description;
    private boolean verified;

    private List<ImageDto> avatars;
    private List<ImageDto> banners;
    private List<ImageDto> parentChannelAvatars;

    private List<TabDto> tabs;
    private List<String> tags;

    // ── Constructors ────────────────────────────────────────────────────────────

    public ChannelDTO() {}

    // ── Static Factory ──────────────────────────────────────────────────────────

    /**
     * Creates a {@code ChannelDTO} from a NewPipe {@link ChannelInfo} object.
     *
     * @param info the extracted channel info
     * @return a fully-populated ChannelDTO
     */
    public static ChannelDTO from(ChannelInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("ChannelInfo cannot be null");
        }

        ChannelDTO dto = new ChannelDTO();

        dto.serviceId = info.getServiceId();
        dto.id        = info.getId();
        dto.url       = info.getUrl();
        dto.originalUrl = info.getOriginalUrl();
        dto.name      = info.getName();
        dto.errors    = List.of(); // NewPipe surfaces errors via exceptions; kept for API consistency

        dto.parentChannelName    = info.getParentChannelName();
        dto.parentChannelUrl     = info.getParentChannelUrl();
        dto.feedUrl              = info.getFeedUrl();
        dto.subscriberCount      = info.getSubscriberCount();
        dto.description          = info.getDescription();
        dto.verified             = info.isVerified();

        dto.avatars = info.getAvatars().stream()
                .map(img -> new ImageDto(
                        img.getUrl(),
                        img.getHeight(),
                        img.getWidth(),
                        img.getEstimatedResolutionLevel().name()
                ))
                .toList();

        dto.banners = info.getBanners().stream()
                .map(img -> new ImageDto(
                        img.getUrl(),
                        img.getHeight(),
                        img.getWidth(),
                        img.getEstimatedResolutionLevel().name()
                ))
                .toList();

        dto.parentChannelAvatars = info.getParentChannelAvatars().stream()
                .map(img -> new ImageDto(
                        img.getUrl(),
                        img.getHeight(),
                        img.getWidth(),
                        img.getEstimatedResolutionLevel().name()
                ))
                .toList();

        dto.tabs = info.getTabs().stream()
                .map(tab -> new TabDto(
                        tab.getOriginalUrl(),
                        tab.getUrl(),
                        tab.getId(),
                        tab.getContentFilters(),
                        tab.getSortFilter(),
                        extractBaseUrl(tab)
                ))
                .toList();

        dto.tags = info.getTags();

        return dto;
    }

    /**
     * Extracts the base URL from a {@link ListLinkHandler}.
     * NewPipe doesn't expose a {@code getBaseUrl()} method directly, so we
     * derive it from the full URL (scheme + host).
     */
    private static String extractBaseUrl(ListLinkHandler tab) {
        try {
            String url = tab.getUrl();
            if (url == null || url.isBlank()) return "";
            java.net.URI uri = java.net.URI.create(url);
            if (uri.getScheme() == null || uri.getHost() == null) return "https://";
            return uri.getScheme() + "://" + uri.getHost();
        } catch (Exception e) {
            return "https://";
        }
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Object> getErrors() { return errors; }
    public void setErrors(List<Object> errors) { this.errors = errors; }

    public String getParentChannelName() { return parentChannelName; }
    public void setParentChannelName(String parentChannelName) { this.parentChannelName = parentChannelName; }

    public String getParentChannelUrl() { return parentChannelUrl; }
    public void setParentChannelUrl(String parentChannelUrl) { this.parentChannelUrl = parentChannelUrl; }

    public String getFeedUrl() { return feedUrl; }
    public void setFeedUrl(String feedUrl) { this.feedUrl = feedUrl; }

    public long getSubscriberCount() { return subscriberCount; }
    public void setSubscriberCount(long subscriberCount) { this.subscriberCount = subscriberCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public List<ImageDto> getAvatars() { return avatars; }
    public void setAvatars(List<ImageDto> avatars) { this.avatars = avatars; }

    public List<ImageDto> getBanners() { return banners; }
    public void setBanners(List<ImageDto> banners) { this.banners = banners; }

    public List<ImageDto> getParentChannelAvatars() { return parentChannelAvatars; }
    public void setParentChannelAvatars(List<ImageDto> parentChannelAvatars) { this.parentChannelAvatars = parentChannelAvatars; }

    public List<TabDto> getTabs() { return tabs; }
    public void setTabs(List<TabDto> tabs) { this.tabs = tabs; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @Override
    public String toString() {
        return "ChannelDTO{id='%s', name='%s', subscriberCount=%d, verified=%b}"
                .formatted(id, name, subscriberCount, verified);
    }
}