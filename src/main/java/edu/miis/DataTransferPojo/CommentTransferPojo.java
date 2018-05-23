package edu.miis.DataTransferPojo;

import java.io.Serializable;

public class CommentTransferPojo implements Serializable{

   private String content;
    private Long articleId;
    private Long commentId;
    private Long authorId;
    private String authorName;

    public CommentTransferPojo(String content, Long articleId, Long commentId, Long authorId, String authorName) {
        this.content = content;
        this.articleId = articleId;
        this.commentId = commentId;
        this.authorId = authorId;
        this.authorName = authorName;
    }

    public String getAuthorName() {

        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public CommentTransferPojo(String content, Long articleId, Long commentId, Long authorId) {
        this.content = content;
        this.articleId = articleId;
        this.commentId = commentId;
        this.authorId = authorId;
    }

    public CommentTransferPojo() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
}
