package edu.miis.repository;

import edu.miis.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    List<User> findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc(String username);
}
