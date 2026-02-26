package org.example.api.controller;

import org.example.api.dto.ChannelDTO;
import org.example.api.service.ChannelService;
import org.example.api.utils.ValidationUtils;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ChannelController {
    private static final String YOUTUBE_URL = "https://www.youtube.com/";
    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);
    private final ChannelService channelService;

    @Autowired
    public ChannelController(ChannelService channelService) { this.channelService = channelService; }

    @GetMapping("/channels")
    public ResponseEntity<?> getChannelInfo(@RequestParam(name = "id") String id) throws Exception {
        logger.info("Retrieving channel info for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        ChannelInfo channel = channelService.getChannelInfo(url);
        ChannelDTO channelDTO = ChannelDTO.from(channel);

        return ResponseEntity.ok(channelDTO);
    }
}
