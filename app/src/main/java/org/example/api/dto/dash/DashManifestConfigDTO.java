package org.example.api.dto.dash;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing the complete DASH manifest configuration.
 * Contains all video, audio, and subtitle streams with manifest metadata.
 * This is the root DTO that holds all streams without filtering.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashManifestConfigDTO {

    /**
     * Type of DASH manifest (typically "static" for VOD content).
     */
    @NotBlank(message = "Manifest type cannot be blank")
    @JsonProperty("type")
    private String type;

    /**
     * Total duration of the media presentation in ISO 8601 format (e.g., "PT1M59.702S").
     */
    @NotBlank(message = "Media presentation duration cannot be blank")
    @JsonProperty("mediaPresentationDuration")
    private String mediaPresentationDuration;

    /**
     * Minimum buffer time required for playback in ISO 8601 format (e.g., "PT2S").
     */
    @NotBlank(message = "Min buffer time cannot be blank")
    @JsonProperty("minBufferTime")
    private String minBufferTime;

    /**
     * DASH profile identifier specifying which DASH features are used.
     */
    @NotBlank(message = "Profiles cannot be blank")
    @JsonProperty("profiles")
    private String profiles;

    /**
     * List of all available video streams with different qualities/resolutions.
     */
    @NotNull(message = "Video streams list cannot be null")
    @JsonProperty("videoStreams")
    private List<VideoStreamMetadataDTO> videoStreams;

    /**
     * List of all available audio streams with different qualities/languages.
     */
    @NotNull(message = "Audio streams list cannot be null")
    @JsonProperty("audioStreams")
    private List<AudioStreamMetadataDTO> audioStreams;

    /**
     * List of all available subtitle tracks.
     */
    @NotNull(message = "Subtitle streams list cannot be null")
    @JsonProperty("subtitleStreams")
    private List<SubtitleMetadataDTO> subtitleStreams;

    /**
     * Duration in seconds (for convenience, not part of XML manifest).
     */
    @Min(value = 1, message = "Duration must be at least 1 second")
    @JsonProperty("durationSeconds")
    private long durationSeconds;

    // Constructors

    /**
     * Default constructor for Jackson deserialization.
     */
    public DashManifestConfigDTO() {
        this.type = "static";
        this.minBufferTime = "PT2S";
        this.profiles = "urn:mpeg:dash:profile:isoff-on-demand:2011";
        this.videoStreams = new ArrayList<>();
        this.audioStreams = new ArrayList<>();
        this.subtitleStreams = new ArrayList<>();
    }

    /**
     * Full constructor for creating a DashManifestConfigDTO with all fields.
     */
    public DashManifestConfigDTO(String type, String mediaPresentationDuration, String minBufferTime,
                                 String profiles, List<VideoStreamMetadataDTO> videoStreams,
                                 List<AudioStreamMetadataDTO> audioStreams,
                                 List<SubtitleMetadataDTO> subtitleStreams, long durationSeconds) {
        this.type = type;
        this.mediaPresentationDuration = mediaPresentationDuration;
        this.minBufferTime = minBufferTime;
        this.profiles = profiles;
        this.videoStreams = videoStreams != null ? videoStreams : new ArrayList<>();
        this.audioStreams = audioStreams != null ? audioStreams : new ArrayList<>();
        this.subtitleStreams = subtitleStreams != null ? subtitleStreams : new ArrayList<>();
        this.durationSeconds = durationSeconds;
    }

    // Static Factory Method

    /**
     * Creates a DashManifestConfigDTO from a NewPipe StreamInfo object.
     * Extracts all available video, audio, and subtitle streams.
     *
     * @param streamInfo The StreamInfo from NewPipe extractor
     * @return A new DashManifestConfigDTO instance with all streams
     */
    public static DashManifestConfigDTO from(StreamInfo streamInfo) {
        if (streamInfo == null) {
            throw new IllegalArgumentException("StreamInfo cannot be null");
        }

        DashManifestConfigDTO dto = new DashManifestConfigDTO();

        // Set duration
        long durationSeconds = streamInfo.getDuration();
        dto.setDurationSeconds(durationSeconds);
        dto.setMediaPresentationDuration(formatDuration(durationSeconds));

        // Extract all video streams
        List<VideoStreamMetadataDTO> videoStreams = new ArrayList<>();
        if (streamInfo.getVideoOnlyStreams() != null) {
            int videoIndex = 1;
            for (var videoStream : streamInfo.getVideoOnlyStreams()) {
                try {
                    videoStreams.add(VideoStreamMetadataDTO.from(videoStream, videoIndex++));
                } catch (Exception e) {
                    // Log and skip invalid streams
                    System.err.println("Skipping invalid video stream: " + e.getMessage());
                }
            }
        }
        dto.setVideoStreams(videoStreams);

        // Extract all audio streams
        List<AudioStreamMetadataDTO> audioStreams = new ArrayList<>();
        if (streamInfo.getAudioStreams() != null) {
            int audioIndex = 1;
            for (var audioStream : streamInfo.getAudioStreams()) {
                try {
                    audioStreams.add(AudioStreamMetadataDTO.from(audioStream, audioIndex++));
                } catch (Exception e) {
                    // Log and skip invalid streams
                    System.err.println("Skipping invalid audio stream: " + e.getMessage());
                }
            }
        }
        dto.setAudioStreams(audioStreams);

        // Extract all subtitle streams
        List<SubtitleMetadataDTO> subtitleStreams = new ArrayList<>();
        if (streamInfo.getSubtitles() != null) {
            int subtitleIndex = 1;
            for (var subtitleStream : streamInfo.getSubtitles()) {
                try {
                    subtitleStreams.add(SubtitleMetadataDTO.from(subtitleStream, subtitleIndex++));
                } catch (Exception e) {
                    // Log and skip invalid streams
                    System.err.println("Skipping invalid subtitle stream: " + e.getMessage());
                }
            }
        }
        dto.setSubtitleStreams(subtitleStreams);

        return dto;
    }

    /**
     * Formats duration in seconds to ISO 8601 duration format (PT#H#M#.###S).
     *
     * @param durationSeconds Duration in seconds
     * @return ISO 8601 formatted duration string
     */
    private static String formatDuration(long durationSeconds) {
        if (durationSeconds <= 0) {
            return "PT0S";
        }

        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        double seconds = durationSeconds % 60;

        StringBuilder duration = new StringBuilder("PT");
        if (hours > 0) {
            duration.append(hours).append("H");
        }
        if (minutes > 0) {
            duration.append(minutes).append("M");
        }
        if (seconds > 0 || (hours == 0 && minutes == 0)) {
            // Format with milliseconds if needed
            if (seconds == (long) seconds) {
                duration.append((long) seconds).append("S");
            } else {
                duration.append(String.format("%.3f", seconds)).append("S");
            }
        }

        return duration.toString();
    }

    // Builder Pattern

    /**
     * Creates a new Builder for constructing DashManifestConfigDTO instances.
     *
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for fluent construction of DashManifestConfigDTO instances.
     */
    public static class Builder {
        private String type = "static";
        private String mediaPresentationDuration;
        private String minBufferTime = "PT2S";
        private String profiles = "urn:mpeg:dash:profile:isoff-on-demand:2011";
        private List<VideoStreamMetadataDTO> videoStreams = new ArrayList<>();
        private List<AudioStreamMetadataDTO> audioStreams = new ArrayList<>();
        private List<SubtitleMetadataDTO> subtitleStreams = new ArrayList<>();
        private long durationSeconds;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder mediaPresentationDuration(String mediaPresentationDuration) {
            this.mediaPresentationDuration = mediaPresentationDuration;
            return this;
        }

        public Builder minBufferTime(String minBufferTime) {
            this.minBufferTime = minBufferTime;
            return this;
        }

        public Builder profiles(String profiles) {
            this.profiles = profiles;
            return this;
        }

        public Builder videoStreams(List<VideoStreamMetadataDTO> videoStreams) {
            this.videoStreams = videoStreams;
            return this;
        }

        public Builder audioStreams(List<AudioStreamMetadataDTO> audioStreams) {
            this.audioStreams = audioStreams;
            return this;
        }

        public Builder subtitleStreams(List<SubtitleMetadataDTO> subtitleStreams) {
            this.subtitleStreams = subtitleStreams;
            return this;
        }

        public Builder durationSeconds(long durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public DashManifestConfigDTO build() {
            return new DashManifestConfigDTO(type, mediaPresentationDuration, minBufferTime,
                    profiles, videoStreams, audioStreams, subtitleStreams, durationSeconds);
        }
    }

    // Getters and Setters

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaPresentationDuration() {
        return mediaPresentationDuration;
    }

    public void setMediaPresentationDuration(String mediaPresentationDuration) {
        this.mediaPresentationDuration = mediaPresentationDuration;
    }

    public String getMinBufferTime() {
        return minBufferTime;
    }

    public void setMinBufferTime(String minBufferTime) {
        this.minBufferTime = minBufferTime;
    }

    public String getProfiles() {
        return profiles;
    }

    public void setProfiles(String profiles) {
        this.profiles = profiles;
    }

    public List<VideoStreamMetadataDTO> getVideoStreams() {
        return videoStreams;
    }

    public void setVideoStreams(List<VideoStreamMetadataDTO> videoStreams) {
        this.videoStreams = videoStreams;
    }

    public List<AudioStreamMetadataDTO> getAudioStreams() {
        return audioStreams;
    }

    public void setAudioStreams(List<AudioStreamMetadataDTO> audioStreams) {
        this.audioStreams = audioStreams;
    }

    public List<SubtitleMetadataDTO> getSubtitleStreams() {
        return subtitleStreams;
    }

    public void setSubtitleStreams(List<SubtitleMetadataDTO> subtitleStreams) {
        this.subtitleStreams = subtitleStreams;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    @Override
    public String toString() {
        return "DashManifestConfigDTO{" +
                "type='" + type + '\'' +
                ", mediaPresentationDuration='" + mediaPresentationDuration + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", videoStreams=" + videoStreams.size() +
                ", audioStreams=" + audioStreams.size() +
                ", subtitleStreams=" + subtitleStreams.size() +
                '}';
    }
}