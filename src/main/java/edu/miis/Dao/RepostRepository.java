package edu.miis.Dao;

import edu.miis.Entities.Repost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepostRepository extends JpaRepository<Repost,Long> {
    boolean existsByRefIdAndForwarderId(Long articleId, Long forwarderId);
}
