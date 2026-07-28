package edu.miis.Dao;

import edu.miis.Entities.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"author", "article"})
    List<Comment> findByArticleIdOrderByIdAsc(Long articleId);
}
