package org.example.api.dto;

import java.util.List;

public class CommentsDTO {
    public record AvatarDto(String url, int height, int width) {}

    public record RepliesDto(String url, String id) {}

    public record UploadDateDto(boolean approximation) {}

    public record CommentItemDto(
            String infoType,
            int serviceId,
            String url,
            String name,
            List<AvatarDto> thumbnails,
            String commentId,
            String commentText,
            int commentTextType,
            String uploaderName,
            List<AvatarDto> uploaderAvatars,
            String uploaderUrl,
            boolean uploaderVerified,
            String textualUploadDate,
            UploadDateDto uploadDate,
            int likeCount,
            String textualLikeCount,
            boolean heartedByUploader,
            boolean pinned,
            int streamPosition,
            int replyCount,
            RepliesDto replies,
            boolean channelOwner
    ) {}

    public record CommentsResponseDto(
            int serviceId,
            String id,
            String url,
            String originalUrl,
            String name,
            List<Object> errors,
            List<CommentItemDto> relatedItems
    ) {}
}
