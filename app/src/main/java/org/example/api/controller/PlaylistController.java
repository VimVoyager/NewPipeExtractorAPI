package org.example.api.controller;

import org.example.api.dto.playlist.PlaylistDTO;
import org.example.api.service.PlaylistService;
import org.example.api.utils.ValidationUtils;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/playlists")
public class PlaylistController {
    private static final String YOUTUBE_PLAYLIST_URL = "https://www.youtube.com/playlist?list=";
    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);
    private final PlaylistService playlistService;

    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ResponseEntity<?> getPlaylistInfo(@RequestParam(name = "id") String id) throws Exception {
        logger.info("Retrieving playlist info for id: {}", id);

        String url = YOUTUBE_PLAYLIST_URL + id;
        ValidationUtils.requireValidUrl(url);

        PlaylistInfo playlist = playlistService.getPlaylistInfo(url);
        PlaylistDTO playlistDTO = PlaylistDTO.from(playlist);
        return ResponseEntity.ok(playlistDTO);
    }

    @GetMapping("/page")
    public ResponseEntity<?> getPlaylistPage(
            @RequestParam(name = "url") String url,
            @RequestParam(name = "pageUrl") String pageUrl
    ) throws Exception {
        logger.info("Retrieving playlist page for url: {}, pageUrl: {}", url, pageUrl);

        ValidationUtils.requireValidUrl(url);
        ValidationUtils.requireValidUrl(pageUrl);

        InfoItemsPage<StreamInfoItem> playlistPage = playlistService.getPlaylistPage(
                url,
                pageUrl
        );

        return ResponseEntity.ok(playlistPage);
    }
}
