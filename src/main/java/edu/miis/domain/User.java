package edu.miis.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
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

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false, length = 120)
    private String securityQuestion;

    @Column(nullable = false, length = 100)
    private String securityAnswerHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected User() {}

    public User(String username, String passwordHash, LocalDate birthday,
                String securityQuestion, String securityAnswerHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.birthday = birthday;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public LocalDate getBirthday() { return birthday; }
    public String getSecurityQuestion() { return securityQuestion; }
    public String getSecurityAnswerHash() { return securityAnswerHash; }
    public Instant getCreatedAt() { return createdAt; }
}
