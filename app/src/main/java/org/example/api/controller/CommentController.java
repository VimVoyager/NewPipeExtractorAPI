package org.example.api.controller;

import org.example.api.dto.CommentsDTO;
import org.example.api.service.CommentService;
import org.example.api.utils.ValidationUtils;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
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
public class CommentController {
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getComments(@RequestParam(name = "id") String id) throws Exception {
        logger.info("Retrieving comments info for ID: {}", id);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);

        CommentsInfo comments = commentService.getCommentsInfo(url);
        CommentsDTO.CommentsResponseDto dto = commentService.mapCommentsToDto(comments);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/comments/page")
    public ResponseEntity<?> getCommentsPage(
            @RequestParam(name = "id") String id,
            @RequestParam(name = "pageUrl") String pageUrl
    ) throws Exception {
        logger.info("Retrieving comments page for id: {}, pageUrl: {}", id, pageUrl);

        String url = YOUTUBE_URL + id;
        ValidationUtils.requireValidUrl(url);
        ValidationUtils.requireValidUrl(pageUrl);

        ListExtractor.InfoItemsPage<CommentsInfoItem> commentsPageJson = commentService.getCommentsPage(
                url,
                pageUrl
        );

        return ResponseEntity.ok(commentsPageJson);
    }
}
