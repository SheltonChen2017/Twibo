package edu.miis.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "security_answer_hash", nullable = false, length = 100)
    private String recoveryPhraseHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected User() {}

    public User(String username, String passwordHash, String recoveryPhraseHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.recoveryPhraseHash = recoveryPhraseHash;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRecoveryPhraseHash() { return recoveryPhraseHash; }
    public Instant getCreatedAt() { return createdAt; }
}
