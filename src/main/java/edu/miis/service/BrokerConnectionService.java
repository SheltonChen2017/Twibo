package edu.miis.service;

import edu.miis.domain.*;
import edu.miis.repository.BrokerConnectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BrokerConnectionService {
    private final BrokerConnectionRepository connections;
    private final TwiboService twibo;

    public BrokerConnectionService(BrokerConnectionRepository connections, TwiboService twibo) {
        this.connections = connections;
        this.twibo = twibo;
    }

    public List<BrokerConnection> connectionsFor(Long ownerId) {
        twibo.requireUser(ownerId);
        return connections.findByOwnerIdOrderByProviderAscEnvironmentAsc(ownerId);
    }

    public boolean hasConnectedPaperAlpaca(Long ownerId) {
        return connections.existsByOwnerIdAndProviderAndEnvironmentAndStatus(
                ownerId, BrokerProvider.ALPACA, BrokerEnvironment.PAPER,
                BrokerConnectionStatus.CONNECTED);
    }
}
