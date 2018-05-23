package edu.miis.DataTransferPojo;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ArticleTransferPojo implements Serializable{

   String authorName;
   Long authorId;
   String content;
   Long articleId;
   Date articleDate;

   public ArticleTransferPojo(String authorName, Long authorId, String content, Long articleId, Date articleDate) {
      this.authorName = authorName;
      this.authorId = authorId;
      this.content = content;
      this.articleId = articleId;
      this.articleDate = articleDate;
   }

   public Date getArticleDate() {
      return articleDate;
   }

   public void setArticleDate(Date articleDate) {
      this.articleDate = articleDate;
   }

   public ArticleTransferPojo() {
   }

   public ArticleTransferPojo(String authorName, Long authorId, String content, Long articleId) {
      this.authorName = authorName;
      this.authorId = authorId;
      this.content = content;
      this.articleId = articleId;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ArticleTransferPojo)) return false;
      ArticleTransferPojo that = (ArticleTransferPojo) o;
      return Objects.equals(getAuthorName(), that.getAuthorName()) &&
              Objects.equals(getAuthorId(), that.getAuthorId()) &&
              Objects.equals(getContent(), that.getContent()) &&
              Objects.equals(getArticleId(), that.getArticleId());
   }

   @Override
   public int hashCode() {

      return Objects.hash(getAuthorName(), getAuthorId(), getContent(), getArticleId());
   }

   public Long getArticleId() {

      return articleId;
   }

   public void setArticleId(Long articleId) {
      this.articleId = articleId;
   }

   public String getAuthorName() {
      return authorName;
   }

   public void setAuthorName(String authorName) {
      this.authorName = authorName;
   }

   public Long getAuthorId() {
      return authorId;
   }

   public void setAuthorId(Long authorId) {
      this.authorId = authorId;
   }

   public String getContent() {
      return content;
   }

   public void setContent(String content) {
      this.content = content;
   }
   //   String[] comments;

}
