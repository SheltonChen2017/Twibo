package edu.miis.DataTransferPojo;

import edu.miis.Entities.Comment;

import java.time.Instant;

public record CommentTransferPojo(
        String content,
        Long articleId,
        Long commentId,
        Long authorId,
        String authorName,
        Instant createdAt
) {
    public static CommentTransferPojo from(Comment comment) {
        return new CommentTransferPojo(
                comment.getContent(),
                comment.getArticle().getId(),
                comment.getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getCreatedAt()
        );
    }
}
