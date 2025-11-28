package org.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import java.util.List;

/**
 * DTO for stream details including metadata like title, description,
 * view counts, likes, channel information, etc.
 */
public class StreamDetailsDTO {

    @JsonProperty("videoTitle")
    private String videoTitle;

    @JsonProperty("description")
    private Description description;

    @JsonProperty("uploaderAvatars")
    private List<Image> uploaderAvatars;

    @JsonProperty("viewCount")
    private long viewCount;

    @JsonProperty("likeCount")
    private long likeCount;

    @JsonProperty("dislikeCount")
    private long dislikeCount;

    @JsonProperty("channelName")
    private String channelName;

    @JsonProperty("channelSubscriberCount")
    private long channelSubscriberCount;

    @JsonProperty("uploadDate")
    private String uploadDate;

    public StreamDetailsDTO() {}

    /**
     * Creates a StreamDetailsDTO from a StreamInfo object.
     *
     * @param streamInfo The StreamInfo from NewPipe extractor
     * @return A new StreamDetailsDTO with mapped data
     */
    public static StreamDetailsDTO from(StreamInfo streamInfo) {
        StreamDetailsDTO dto = new StreamDetailsDTO();

        dto.setVideoTitle(streamInfo.getName());
        dto.setDescription(streamInfo.getDescription());
        dto.setUploaderAvatars(streamInfo.getUploaderAvatars());
        dto.setViewCount(streamInfo.getViewCount());
        dto.setLikeCount(streamInfo.getLikeCount());
        dto.setDislikeCount(streamInfo.getDislikeCount());
        dto.setChannelName(streamInfo.getUploaderName());
        dto.setChannelSubscriberCount(streamInfo.getUploaderSubscriberCount());
        dto.setUploadDate(streamInfo.getTextualUploadDate());

        return dto;
    }

    // Getters and Setters
    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public List<Image> getUploaderAvatars() {
        return uploaderAvatars;
    }

    public void setUploaderAvatars(List<Image> uploaderAvatars) {
        this.uploaderAvatars = uploaderAvatars;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(long dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public long getChannelSubscriberCount() {
        return channelSubscriberCount;
    }

    public void setChannelSubscriberCount(long channelSubscriberCount) {
        this.channelSubscriberCount = channelSubscriberCount;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
}