package edu.miis.repository;

import edu.miis.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = "author")
    @Query("""
        select p from Post p
        where p.author.id = :userId
           or p.author.id in (select f.followed.id from Follow f where f.follower.id = :userId)
        """)
    Page<Post> findFeed(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<Post> findById(Long id);
}
