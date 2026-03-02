package org.example.api.controller;

import org.example.api.dto.channels.ChannelDTO;
import org.example.api.dto.channels.ChannelTabDTO;
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

import java.util.List;

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

    @GetMapping("/tab")
    public ResponseEntity<ChannelTabDTO> getChannelTab(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "tab", defaultValue = ChannelTabs.VIDEOS) String tab
    ) throws ExtractionException {
        logger.info("Fetching channel tab '{}' for ID: {}", tab, id);

        String channelUrl = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(channelUrl);

        ChannelTabDTO result = channelTabService.getChannelTab(channelUrl, tab, id);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns a subsequent page of channel tab items.
     *
     * <p>All three {@code nextPage.*} parameters must be taken verbatim from the
     * {@code nextPage} object in the previous response — the body and ids are required
     * by NewPipe to correctly make the continuation request and annotate returned items.</p>
     *
     * @param channelId  channel ID from the previous response's {@code channelId} field
     * @param tab        tab type — must match the initial request
     * @param pageUrl    from {@code nextPage.url}
     * @param pageBody   from {@code nextPage.body} (Base64-encoded continuation token)
     * @param pageIds    from {@code nextPage.ids} (["channelName", "channelUrl", "verifiedStatus"])
     */
    @GetMapping("/tab/page")
    public ResponseEntity<ChannelTabDTO> getChannelTabPage(
            @RequestParam(name = "channelId") String channelId,
            @RequestParam(name = "tab", defaultValue = ChannelTabs.VIDEOS) String tab,
            @RequestParam(name = "pageUrl") String pageUrl,
            @RequestParam(name = "pageBody") String pageBody,
            @RequestParam(name = "pageIds") List<String> pageIds
    ) throws Exception {
        logger.info("Retrieving channel tab '{}' page for channelId: {}", tab, channelId);

        ChannelTabDTO result = channelTabService.getChannelTabPage(
                channelId, tab, pageUrl, pageBody, pageIds);
        return ResponseEntity.ok(result);
    }
}