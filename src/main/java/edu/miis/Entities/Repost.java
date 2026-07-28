package edu.miis.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "repost",
        uniqueConstraints = @UniqueConstraint(name = "uk_repost_article_user", columnNames = {"ref_id", "forwarder_id"})
)
public class Repost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ref_id", nullable = false)
    private Article ref;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forwarder_id", nullable = false)
    private UserBean forwarder;

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public Article getRef() {
        return ref;
    }

    public void setRef(Article ref) {
        this.ref = ref;
    }

    public UserBean getForwarder() {
        return forwarder;
    }

    public void setForwarder(UserBean forwarder) {
        this.forwarder = forwarder;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
