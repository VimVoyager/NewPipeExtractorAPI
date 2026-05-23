package org.example.api.service;

import org.example.api.exception.ExtractionException;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for extracting detailed information about video streams.
 * Returns DTOs/domain objects instead of JSON strings for better separation of concerns.
 */
@Service
public class VideoStreamingService {
    private static final Logger logger = LoggerFactory.getLogger(VideoStreamingService.class);

    /**
     * Retrieves comprehensive information about a specific stream.
     *
     * @param url The URL of the stream
     * @return StreamInfo object with complete stream information
     * @throws ExtractionException if extraction fails
     */
    public StreamInfo getStreamInfo(String url) throws ExtractionException {
        return getStreamInfo(url, 2);
    }

    /**
     * Extracts StreamInfo with automatic retry when YouTube returns an incomplete
     * response (0 video-only streams, 0 audio streams, but valid duration).
     *
     * This happens because NewPipe uses multiple internal client extractors
     * (ANDROID, WEB, etc.) and non-deterministically hits a client that returns
     * only a muxed stream instead of the full adaptive stream set.
     * A single retry is sufficient — the second call almost always hits a
     * different client path and returns the full stream list.
     */

    private StreamInfo getStreamInfo(String url, int attemptsRemaining) throws ExtractionException {
        try {
            logger.info("Extracting stream info for URL: {}", url);
            StreamInfo info = StreamInfo.getInfo(url);

            boolean hasAdaptiveStreams = !info.getVideoOnlyStreams().isEmpty() || !info.getAudioStreams().isEmpty();
            boolean isValidResult = info.getDuration() > 0;

            if (!hasAdaptiveStreams && isValidResult && attemptsRemaining > 1) {
                logger.warn("Got incomplete streams for URL: {} (videoOnly={}, audio={}, muxed={}) — retrying ({} attempts remaining)",
                        url,
                        info.getVideoOnlyStreams().size(),
                        info.getAudioStreams().size(),
                        info.getVideoStreams().size(),
                        attemptsRemaining - 1);
                return getStreamInfo(url, attemptsRemaining - 1);
            }
            logger.info("Extracted streams for URL: {} (videoOnly={}, audio={}, muxed={})",
                    url,
                    info.getVideoOnlyStreams().size(),
                    info.getAudioStreams().size(),
                    info.getVideoStreams().size());

            return info;
        } catch (Exception e) {
            logger.error("Failed to extract stream info for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Extracts available audio streams for a given stream URL.
     *
     * @param url The URL of the stream
     * @return List of audio streams
     * @throws ExtractionException if extraction fails
     */
    public List<AudioStream> getAudioStreams(String url) throws ExtractionException{
        try {
            logger.info("Extracting audio streams for URL: {}", url);
            return StreamInfo.getInfo(url).getAudioStreams();
        } catch (Exception e) {
            logger.error("Failed to extract audio streams for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Extracts available video streams for a given stream URL.
     *
     * @param url The URL of the stream
     * @return List of video streams
     * @throws ExtractionException if extraction fails
     */
    public List<VideoStream> getVideoStreams(String url) throws ExtractionException {
        try {
            logger.info("Extracting video streams for URL: {}", url);
            return StreamInfo.getInfo(url).getVideoOnlyStreams();
        } catch (Exception e) {
            logger.error("Failed to extract video streams for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Extracts the DASH MPD URL for a given stream.
     *
     * @param url The URL of the stream
     * @return DASH MPD URL string
     * @throws ExtractionException if extraction fails
     */
    public String getDashMpdUrl(String url) throws ExtractionException {
        try {
            logger.info("Extracting DASH MPD URL for: {}", url);
            String dashUrl = StreamInfo.getInfo(url).getDashMpdUrl();
            logger.debug("DASH MPD URL: {}", dashUrl);
            return dashUrl;
        } catch (Exception e) {
            logger.error("Failed to extract DASH MPD URL for: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Extracts thumbnail images for a given stream URL
     *
     * @param url The URL of the stream
     * @return List of subtitle streams
     * @throws ExtractionException if extraction fails
     */
    public List<Image> getStreamThumbnails(String url) throws ExtractionException {
        try {
            logger.info("Extracting stream thumbnails for URL: {}", url);
            return StreamInfo.getInfo(url).getThumbnails();
        } catch (Exception e) {
            logger.error("Failed to extract subtitle streams for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Extracts available subtitle streams for a given stream URL.
     *
     * @param url The URL of the stream
     * @return List of subtitle streams
     * @throws ExtractionException if extraction fails
     */
    public List<SubtitlesStream> getSubtitleStreams(String url) throws ExtractionException {
        try {
            logger.info("Extracting subtitle streams for URL: {}", url);
            return StreamInfo.getInfo(url).getSubtitles();
        } catch (Exception e) {
            logger.error("Failed to extract subtitle streams for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Extracts stream segments for a given stream URL.
     *
     * @param url The URL of the stream
     * @return List of stream segments
     * @throws ExtractionException if extraction fails
     */
    public List<StreamSegment> getStreamSegments(String url) throws ExtractionException {
        try {
            logger.info("Extracting stream segments for URL: {}", url);
            return StreamInfo.getInfo(url).getStreamSegments();
        } catch (Exception e) {
            logger.error("Failed to extract stream segments for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Extracts preview frames for a given stream URL.
     *
     * @param url The URL of the stream
     * @return List of preview framesets
     * @throws ExtractionException if extraction fails
     */
    public List<Frameset> getPreviewFrames(String url) throws ExtractionException {
        try {
            logger.info("Extracting preview frames for URL: {}", url);
            return StreamInfo.getInfo(url).getPreviewFrames();
        } catch (Exception e) {
            logger.error("Failed to extract preview frames for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the description of a stream for a given URL.
     *
     * @param url The URL of the stream
     * @return Description object
     * @throws ExtractionException if extraction fails
     */
    public Description getStreamDescription(String url) throws ExtractionException {
        try {
            logger.info("Extracting stream description for URL: {}", url);
            return StreamInfo.getInfo(url).getDescription();
        } catch (Exception e) {
            logger.error("Failed to extract stream description for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves related streams for a given URL.
     *
     * @param url The URL of the stream
     * @return List of related items
     * @throws ExtractionException if extraction fails
     */
    public List<InfoItem> getRelatedStreams(String url) throws ExtractionException {
        try {
            logger.info("Extracting related streams for URL: {}", url);
            return StreamInfo.getInfo(url).getRelatedItems();
        } catch (Exception e) {
            logger.error("Failed to extract related streams for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }
}
