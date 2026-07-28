package edu.miis.Dao;

import edu.miis.Entities.Relationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RelationshipRepository extends JpaRepository<Relationship, Long> {

    Optional<Relationship> findByUserIdAndFid(Long userId, Long followedId);

    boolean existsByUserIdAndFid(Long userId, Long followedId);

    @Query("select r.fid from Relationship r where r.user.id = :userId")
    List<Long> findFollowedIds(@Param("userId") Long userId);
}
