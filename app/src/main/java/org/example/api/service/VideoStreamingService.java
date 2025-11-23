package org.example.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.stream.StreamExtractor.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.api.utils.ErrorUtils.getError;

/**
 * Service for extracting detailed information about video streams.
 *
 * Provides comprehensive methods to retrieve various aspects of
 * stream information using the NewPipe extractor, including audio
 * streams, video streams, subtitles, segments, and other metadata.
 */
@Service
public class VideoStreamingService {
    private final ObjectMapper objectMapper;

    public VideoStreamingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves comprehensive information about a specific stream.
     *
     * Extracts and serializes full stream information for a given URL,
     * handling potential extraction errors with a standardized error response.
     *
     * @param url The URL of the stream to retrieve information for
     * @return A JSON string containing stream information or an error message
     * @throws IOException If an I/O error occurs during extraction
     * @throws ExtractionException If an error occurs during stream information extraction
     */
    public String getStreamInfo(String url) throws IOException, ExtractionException {
        try {
            StreamInfo info = StreamInfo.getInfo(url);
            return objectMapper.writeValueAsString(info);
        } catch (Exception e) {
            System.err.println("Stream Info Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    /**
     * Extracts available audio streams for a given stream URL.
     *
     * Retrieves and serializes a list of audio streams associated
     * with the specified stream URL.
     *
     * @param url The URL of the stream to extract audio streams from
     * @return A JSON string containing audio streams or an error message
     * @throws IOException If an I/O error occurs during extraction
     * @throws ExtractionException If an error occurs during stream information extraction
     */
    public String getAudioStreams(String url) throws IOException, ExtractionException {
        try {
            List<AudioStream> audioStreams = StreamInfo.getInfo(url).getAudioStreams();
            return objectMapper.writeValueAsString(audioStreams);
        } catch (Exception e) {
            System.err.println("Audio Stream Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    /**
     * Extracts available video streams for a given stream URL.
     *
     * Retrieves and serializes a list of video streams associated
     * with the specified stream URL.
     *
     * @param url The URL of the stream to extract video streams from
     * @return A JSON string containing video streams or an error message
     * @throws IOException If an I/O error occurs during extraction
     * @throws ExtractionException If an error occurs during stream information extraction
     */
    public String getVideoStreams(String url) throws IOException, ExtractionException {
        try {
            List<VideoStream> videoStreams = StreamInfo.getInfo(url).getVideoOnlyStreams();
            return objectMapper.writeValueAsString(videoStreams);
        } catch (Exception e) {
            System.err.println("Video Stream Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }


    public String getDashMpdUrl(String url) throws IOException, ExtractionException {
        try {
            String stream = StreamInfo.getInfo(url).getDashMpdUrl();
            System.out.println("Stream" + stream);
            return objectMapper.writeValueAsString(stream);
        } catch (Exception e) {
            System.err.println("Video Stream Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    /**
     * Extracts available subtitle streams for a given stream URL.
     *
     * Retrieves and serializes a list of subtitle streams associated
     * with the specified stream URL.
     *
     * @param url The URL of the stream to extract subtitle streams from
     * @return A JSON string containing subtitle streams or an error message
     * @throws IOException If an I/O error occurs during extraction
     * @throws ExtractionException If an error occurs during stream information extraction
     */
    public String getSubtitleStreams(String url) throws IOException, ExtractionException {
        try {
            List<SubtitlesStream> subtitleStreams = StreamInfo.getInfo(url).getSubtitles();
            return objectMapper.writeValueAsString(subtitleStreams);
        } catch (Exception e) {
            System.err.println("Subtitles Stream Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    /**
     * Extracts stream segments for a given stream URL.
     *
     * Retrieves and serializes a list of stream segments associated
     * with the specified stream URL. Stream segments can represent
     * different parts or chapters of a media stream.
     *
     * @param url The URL of the stream to extract segments from
     * @return A JSON string containing stream segments or an error message
     * @throws IOException If an I/O error occurs during extraction
     * @throws ExtractionException If an error occurs during stream information extraction
     */
    public String getStreamSegments(String url) throws IOException, ExtractionException {
        try {
            List<StreamSegment> segments = StreamInfo.getInfo(url).getStreamSegments();
            return objectMapper.writeValueAsString(segments);
        } catch (Exception e) {
            System.err.println("Stream segments extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    /**
     * Extracts preview frames for a given stream URL.
     *
     * Retrieves and serializes a list of preview framesets associated
     * with the specified stream URL. These framesets can be used for
     * generating thumbnails or quick video previews.
     *
     * @param url The URL of the stream to extract preview frames from
     * @return A JSON string containing preview framesets or an error message
     * @throws IOException If an I/O error occurs during extraction
     * @throws ExtractionException If an error occurs during stream information extraction
     */
    public String getPreviewFrames(String url) throws IOException, ExtractionException {
        try {
            List<Frameset> framesets = StreamInfo.getInfo(url).getPreviewFrames();
            return objectMapper.writeValueAsString(framesets);
        } catch (Exception e) {
            System.err.println("Preview Frames Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    /**
     * Retrieves the description of a stream for a given URL.
     *
     * Extracts and serializes the textual description associated
     * with the specified stream URL. This can provide additional
     * context or metadata about the stream content.
     *
     * @param url The URL of the stream to extract description from
     * @return A JSON string containing the stream description or an error message
     * @throws IOException If an I/O error occurs during extraction
     * @throws ExtractionException If an error occurs during stream information extraction
     */
    public String getStreamDescription(String url) throws IOException, ExtractionException {
        try {
            Description streamDescription = StreamInfo.getInfo(url).getDescription();
            return objectMapper.writeValueAsString(streamDescription);
        } catch (Exception e) {
            System.err.println("Description Stream Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

    /**
     * Retrieves the video details of a stream for a given URL.
     *
     * Extracts and serializes the description, avatars, view count,
     * like count, dislike count, channel name, channel subscriber count
     * upload date with the specified stream URL.
     *
     * @param url The URL of the stream to extract stream details from
     * @return A JSON string containing the stream details or an error message
     * @throws IOException If an I/O error occurs during extraction
     * @throws ExtractionException If an error occurs during stream information extraction
     */
    public String getStreamDetails(String url) throws IOException, ExtractionException {
        try {
            String videoTitle = StreamInfo.getInfo(url).getName();
            Description description = StreamInfo.getInfo(url).getDescription();
            List<Image> avatars = StreamInfo.getInfo(url).getUploaderAvatars();
            long viewCount = StreamInfo.getInfo(url).getViewCount();
            long likeCount = StreamInfo.getInfo(url).getLikeCount();
            long dislikeCount = StreamInfo.getInfo(url).getDislikeCount();
            String channelName = StreamInfo.getInfo(url).getUploaderName();
            long channelSubscriberCount = StreamInfo.getInfo(url).getUploaderSubscriberCount();
            String uploadDate = StreamInfo.getInfo(url).getTextualUploadDate();

            // Map to build the JSON
            Map<String, Object> details = new HashMap<>();
            details.put("videoTitle", videoTitle);
            details.put("description", description);
            details.put("uploaderAvatars", avatars);
            details.put("viewCount", viewCount);
            details.put("likeCount", likeCount);
            details.put("dislikeCount", dislikeCount);
            details.put("channelName", channelName);
            details.put("channelSubscriberCount", channelSubscriberCount);
            details.put("uploadDate", uploadDate);

            return objectMapper.writeValueAsString(details);

        } catch (Exception e) {
            System.err.println("Details extraction error");
            e.printStackTrace();
            return getError(e);
        }
    }

    public String getRelatedStreams(String url) throws IOException, ExtractionException {
        try {
            List<InfoItem> relatedItems = StreamInfo.getInfo(url).getRelatedItems();
            return objectMapper.writeValueAsString(relatedItems);
        } catch (Exception e) {
            System.err.println("Related streams extraction error");
            e.printStackTrace();
            return getError(e);
        }
    }
}
