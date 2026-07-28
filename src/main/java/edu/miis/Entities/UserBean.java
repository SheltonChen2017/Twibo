package edu.miis.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user_info")
public class UserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 35, unique = true, nullable = false)
    private String username;

    @JsonIgnore
    @Column(length = 100, nullable = false)
    private String password;

    @Column(name = "DateOfBirth", nullable = false)
    private LocalDate birthday;

    @Column(name = "profile_photo", length = 255)
    private String profilePhoto;

    @Column(name = "last_login_time")
    private Instant lastLoginTime = Instant.now();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Article> articles = new HashSet<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Relationship> relationships = new HashSet<>();

    @OneToMany(mappedBy = "forwarder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Repost> reposts = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SecurityQuestion> securityQuestions = new HashSet<>();

    public void addArticle(Article article) {
        articles.add(article);
        article.setAuthor(this);
    }

    public void addSecurityQuestion(SecurityQuestion securityQuestion) {
        securityQuestions.add(securityQuestion);
        securityQuestion.setOwner(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Instant getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Instant lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public Set<Relationship> getRelationships() {
        return relationships;
    }

    public Set<Repost> getReposts() {
        return reposts;
    }

    public Set<SecurityQuestion> getSecurityQuestions() {
        return securityQuestions;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserBean user)) {
            return false;
        }
        return username != null && username.equalsIgnoreCase(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username == null ? null : username.toLowerCase(Locale.ROOT));
    }
}
