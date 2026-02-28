package org.example.api.controller;

import org.example.api.dto.ChannelDTO;
import org.example.api.dto.ChannelTabDTO;
import org.example.api.exception.ExtractionException;
import org.example.api.service.ChannelService;
import org.example.api.service.ChannelTabService;
import org.example.api.utils.ValidationUtils;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channels")
public class ChannelController {
    private static final String YOUTUBE_URL = "https://www.youtube.com/";
    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);

    private final ChannelService channelService;
    private final ChannelTabService channelTabService;

    @Autowired
    public ChannelController(ChannelService channelService, ChannelTabService channelTabService) {
        this.channelService = channelService;
        this.channelTabService = channelTabService;
    }

    @GetMapping
    public ResponseEntity<?> getChannelInfo(@RequestParam(name = "id") String id) throws Exception {
        logger.info("Retrieving channel info for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        ChannelInfo channel = channelService.getChannelInfo(url);
        ChannelDTO channelDTO = ChannelDTO.from(channel);

        return ResponseEntity.ok(channelDTO);
    }

    @GetMapping("/{id}/tab")
    public ResponseEntity<ChannelTabDTO> getChannelTab(
            @PathVariable String id,
            @RequestParam(name = "tab", defaultValue = ChannelTabs.VIDEOS) String tab
    ) throws ExtractionException {
        logger.info("Fetching channel tab '{}' for id: {}", tab, id);

        String channelUrl = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(channelUrl);

        ChannelTabDTO result = channelTabService.getChannelTab(channelUrl, tab, id);
        return ResponseEntity.ok(result);
    }


}
