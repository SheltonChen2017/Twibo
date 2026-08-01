package edu.miis.repository;

import edu.miis.domain.BrokerConnection;
import edu.miis.domain.BrokerConnectionStatus;
import edu.miis.domain.BrokerEnvironment;
import edu.miis.domain.BrokerProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrokerConnectionRepository extends JpaRepository<BrokerConnection, Long> {
    List<BrokerConnection> findByOwnerIdOrderByProviderAscEnvironmentAsc(Long ownerId);

    Optional<BrokerConnection> findByOwnerIdAndProviderAndEnvironment(
            Long ownerId, BrokerProvider provider, BrokerEnvironment environment);

    boolean existsByOwnerIdAndProviderAndEnvironmentAndStatus(
            Long ownerId, BrokerProvider provider, BrokerEnvironment environment,
            BrokerConnectionStatus status);
}
