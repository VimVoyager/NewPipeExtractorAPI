package org.example.api.service;

import org.example.api.exception.ExtractionException;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PlaylistService {
    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    public PlaylistInfo getPlaylistInfo(String url) throws Exception {
        try {
            logger.info("Extracting playlist info for URL {}:", url);
            return PlaylistInfo.getInfo(url);
        } catch (Exception e) {
            logger.error("Failed to extract playlist info for URL {}:", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

    public InfoItemsPage<StreamInfoItem> getPlaylistPage( String url, String pageUrl) {
        try {
            StreamingService service = NewPipe.getServiceByUrl(url);
            Page pageInstance = new Page(pageUrl);
            logger.error("Failed to extract playlist page for URL {}:", url);
            return PlaylistInfo.getMoreItems(service, url, pageInstance);
        } catch (Exception e) {
            logger.error("Failed to extract playlist page for URL {}:", url, e);
            throw new ExtractionException(e.getMessage(), e);
        }
    }

}
