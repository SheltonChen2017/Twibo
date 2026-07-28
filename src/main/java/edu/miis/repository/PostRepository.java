package edu.miis.repository;

import edu.miis.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
        select distinct p from Post p join fetch p.author
        where p.author.id = :userId
           or p.author.id in (select f.followed.id from Follow f where f.follower.id = :userId)
        order by p.createdAt desc
        """)
    List<Post> findFeed(@Param("userId") Long userId);

    @EntityGraph(attributePaths = "author")
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @Override
    @EntityGraph(attributePaths = "author")
    Optional<Post> findById(Long id);
}
