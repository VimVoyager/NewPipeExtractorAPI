package org.example.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.stream.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static org.example.api.utils.ErrorUtils.getError;

@Service
public class VideoStreamingService {
    private final ObjectMapper objectMapper;

    public VideoStreamingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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

    public String getVideoStreams(String url) throws IOException, ExtractionException {
        try {
            List<VideoStream> videoStreams = StreamInfo.getInfo(url).getVideoStreams();
            return objectMapper.writeValueAsString(videoStreams);
        } catch (Exception e) {
            System.err.println("Video Stream Extraction Error:");
            e.printStackTrace();
            return getError(e);
        }
    }

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
}
