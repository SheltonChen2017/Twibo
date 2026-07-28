package edu.miis.Dao;

import edu.miis.Entities.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ArticlesRepository extends JpaRepository<Article, Long> {

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<Article> findById(Long id);

    @EntityGraph(attributePaths = "author")
    Page<Article> findByAuthorIdIn(Collection<Long> authorIds, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Article> findByAuthorId(Long authorId, Pageable pageable);
}
