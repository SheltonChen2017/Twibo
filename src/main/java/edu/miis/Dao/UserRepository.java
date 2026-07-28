package edu.miis.Dao;

import edu.miis.Entities.UserBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserBean, Long> {

    Optional<UserBean> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "securityQuestions")
    @Query("select u from UserBean u where lower(u.username) = lower(:username)")
    Optional<UserBean> findWithSecurityQuestions(@Param("username") String username);

    @Query("select u from UserBean u where lower(u.username) like lower(concat('%', :name, '%')) order by u.username")
    List<UserBean> searchByUsername(@Param("name") String name, Pageable pageable);
}
