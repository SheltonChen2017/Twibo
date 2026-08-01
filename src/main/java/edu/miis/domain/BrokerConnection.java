package edu.miis.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "broker_connection", uniqueConstraints = @UniqueConstraint(
        name = "uk_broker_connection_owner_provider_env",
        columnNames = {"owner_id", "provider", "environment"}))
public class BrokerConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BrokerProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BrokerEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BrokerConnectionStatus status;

    @Column(name = "external_account_label", length = 80)
    private String externalAccountLabel;

    @Column(name = "granted_scopes", nullable = false, length = 255)
    private String grantedScopes = "";

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected BrokerConnection() {}

    public BrokerConnection(User owner, BrokerProvider provider, BrokerEnvironment environment) {
        this.owner = owner;
        this.provider = provider;
        this.environment = environment;
        this.status = BrokerConnectionStatus.PENDING;
    }

    @PreUpdate
    void updateTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public BrokerProvider getProvider() { return provider; }
    public BrokerEnvironment getEnvironment() { return environment; }
    public BrokerConnectionStatus getStatus() { return status; }
    public String getExternalAccountLabel() { return externalAccountLabel; }
    public String getGrantedScopes() { return grantedScopes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isConnected() {
        return status == BrokerConnectionStatus.CONNECTED;
    }
}
