package edu.miis.DataTransferPojo;

import edu.miis.Entities.Article;

import java.time.Instant;

public record ArticleTransferPojo(
        String authorName,
        Long authorId,
        String content,
        Long articleId,
        Instant articleDate
) {
    public static ArticleTransferPojo from(Article article) {
        return new ArticleTransferPojo(
                article.getAuthor().getUsername(),
                article.getAuthor().getId(),
                article.getContent(),
                article.getId(),
                article.getInsertTime()
        );
    }
}
