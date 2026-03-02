package org.example.api.service;

import org.example.api.exception.ExtractionException;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChannelService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    public ChannelInfo getChannelInfo(String url) throws ExtractionException {
        try {
            logger.info("Extracting channel info for URL: {}", url);
            return ChannelInfo.getInfo(url);
        } catch (Exception e) {
            logger.error("Failed to extract channel info for URL: {}", url, e);
            throw new ExtractionException(e.getMessage(), e.getCause());
        }
    }
}
