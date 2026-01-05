package org.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for individual search result items.
 * Handles different types of search results (videos, channels, playlists).
 */
public class SearchItemDTO {

    @JsonProperty("type")
    private String type; // "stream", "channel", "playlist"

    @JsonProperty("name")
    private String name;

    @JsonProperty("url")
    private String url;

    @JsonProperty("thumbnailUrl")
    private String thumbnailUrl;

    // Stream-specific fields
    @JsonProperty("uploaderName")
    private String uploaderName;

    @JsonProperty("uploaderUrl")
    private String uploaderUrl;

    @JsonProperty("uploaderAvatarUrl")
    private String uploaderAvatarUrl;

    @JsonProperty("uploaderVerified")
    private Boolean uploaderVerified;

    @JsonProperty("duration")
    private Long duration;

    @JsonProperty("viewCount")
    private Long viewCount;

    @JsonProperty("uploadDate")
    private String uploadDate;

    @JsonProperty("streamType")
    private String streamType; // "VIDEO_STREAM", "AUDIO_STREAM", "LIVE_STREAM", etc.

    @JsonProperty("isShortFormContent")
    private Boolean isShortFormContent;

    // Channel-specific fields
    @JsonProperty("subscriberCount")
    private Long subscriberCount;

    @JsonProperty("streamCount")
    private Long streamCount;

    @JsonProperty("description")
    private String description;

    // Playlist-specific fields
    @JsonProperty("playlistType")
    private String playlistType;

    @JsonProperty("videoCount")
    private Long videoCount;

    // Constructors
    public SearchItemDTO() {}

    /**
     * Creates a SearchItemDTO from an InfoItem.
     *
     * @param item The InfoItem from NewPipe extractor
     * @return A new SearchItemDTO with mapped data
     */
    public static SearchItemDTO from(InfoItem item) {
        SearchItemDTO dto = new SearchItemDTO();

        // Common fields
        dto.setName(item.getName());
        dto.setUrl(item.getUrl());

        // Get thumbnail URL (first available thumbnail)
        if (!item.getThumbnails().isEmpty()) {
            dto.setThumbnailUrl(item.getThumbnails().getFirst().getUrl());
        }

        // Map based on item type
        switch (item) {
            case StreamInfoItem streamInfoItem -> {
                dto.setType("stream");
                mapStreamInfo(dto, streamInfoItem);
            }
            case ChannelInfoItem channelInfoItem -> {
                dto.setType("channel");
                mapChannelInfo(dto, channelInfoItem);
            }
            case PlaylistInfoItem playlistInfoItem -> {
                dto.setType("playlist");
                mapPlaylistInfo(dto, playlistInfoItem);
            }
            default -> {
            }
        }

        return dto;
    }

    /**
     * Maps stream-specific information.
     */
    private static void mapStreamInfo(SearchItemDTO dto, StreamInfoItem streamItem) {
        dto.setUploaderName(streamItem.getUploaderName());
        dto.setUploaderUrl(streamItem.getUploaderUrl());
        dto.setUploaderVerified(streamItem.isUploaderVerified());

        // Get uploader avatar
        if (!streamItem.getUploaderAvatars().isEmpty()) {
            dto.setUploaderAvatarUrl(streamItem.getUploaderAvatars().getFirst().getUrl());
        }

        dto.setDuration(streamItem.getDuration());
        dto.setViewCount(streamItem.getViewCount());

        // Handle upload date
        if (streamItem.getUploadDate() != null) {
            dto.setUploadDate(streamItem.getUploadDate().offsetDateTime().toString());
        }

        // Stream type
        StreamType streamType = streamItem.getStreamType();
        if (streamType != null) {
            dto.setStreamType(streamType.name());
        }

        dto.setShortFormContent(streamItem.isShortFormContent());
    }

    /**
     * Maps channel-specific information.
     */
    private static void mapChannelInfo(SearchItemDTO dto, ChannelInfoItem channelItem) {
        dto.setSubscriberCount(channelItem.getSubscriberCount());
        dto.setStreamCount(channelItem.getStreamCount());
        dto.setDescription(channelItem.getDescription());
        dto.setUploaderVerified(channelItem.isVerified());
    }

    /**
     * Maps playlist-specific information.
     */
    private static void mapPlaylistInfo(SearchItemDTO dto, PlaylistInfoItem playlistItem) {
        dto.setUploaderName(playlistItem.getUploaderName());
        dto.setUploaderUrl(playlistItem.getUploaderUrl());
        dto.setVideoCount(playlistItem.getStreamCount());

        if (playlistItem.getPlaylistType() != null) {
            dto.setPlaylistType(playlistItem.getPlaylistType().name());
        }
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderUrl() {
        return uploaderUrl;
    }

    public void setUploaderUrl(String uploaderUrl) {
        this.uploaderUrl = uploaderUrl;
    }

    public String getUploaderAvatarUrl() {
        return uploaderAvatarUrl;
    }

    public void setUploaderAvatarUrl(String uploaderAvatarUrl) {
        this.uploaderAvatarUrl = uploaderAvatarUrl;
    }

    public Boolean getUploaderVerified() {
        return uploaderVerified;
    }

    public void setUploaderVerified(Boolean uploaderVerified) {
        this.uploaderVerified = uploaderVerified;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public Boolean getShortFormContent() {
        return isShortFormContent;
    }

    public void setShortFormContent(Boolean shortFormContent) {
        isShortFormContent = shortFormContent;
    }

    public Long getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(Long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public Long getStreamCount() {
        return streamCount;
    }

    public void setStreamCount(Long streamCount) {
        this.streamCount = streamCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaylistType() {
        return playlistType;
    }

    public void setPlaylistType(String playlistType) {
        this.playlistType = playlistType;
    }

    public Long getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(Long videoCount) {
        this.videoCount = videoCount;
    }
}