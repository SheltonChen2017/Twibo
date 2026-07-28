package edu.miis.service;

import edu.miis.domain.*;
import edu.miis.repository.*;
import edu.miis.web.SignupForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TwiboService {
    private final UserRepository users;
    private final PostRepository posts;
    private final CommentRepository comments;
    private final FollowRepository follows;
    private final PasswordEncoder encoder;
    private final String dummyPasswordHash;

    public TwiboService(UserRepository users, PostRepository posts, CommentRepository comments,
                        FollowRepository follows, PasswordEncoder encoder) {
        this.users = users;
        this.posts = posts;
        this.comments = comments;
        this.follows = follows;
        this.encoder = encoder;
        this.dummyPasswordHash = encoder.encode(UUID.randomUUID().toString());
    }

    public User register(SignupForm form) {
        String username = form.getUsername().trim();
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        String password = validatedPassword(form.getPassword());
        return users.save(new User(username, encoder.encode(password),
                encoder.encode(validatedRecoveryPhrase(form.getRecoveryPhrase()))));
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String username, String password) {
        String normalized = normalizedUsername(username);
        Optional<User> user = normalized.length() <= 30
                ? users.findByUsernameIgnoreCase(normalized)
                : Optional.empty();
        String passwordHash = user.map(User::getPasswordHash).orElse(dummyPasswordHash);
        boolean validLength = fitsBcrypt(password);
        boolean matches = encoder.matches(validLength ? password : "invalid-over-limit", passwordHash);
        return validLength && matches ? user : Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<User> verifyRecoveryPhrase(String username, String phrase) {
        String normalizedUsername = normalizedUsername(username);
        Optional<User> user = normalizedUsername.length() <= 30
                ? users.findByUsernameIgnoreCase(normalizedUsername)
                : Optional.empty();
        String answerHash = user.map(User::getRecoveryPhraseHash).orElse(dummyPasswordHash);
        String normalizedPhrase = normalizeAnswer(phrase);
        boolean validLength = fitsBcrypt(normalizedPhrase);
        boolean matches = encoder.matches(validLength ? normalizedPhrase : "invalid-over-limit", answerHash);
        return validLength && matches ? user : Optional.empty();
    }

    @Transactional(readOnly = true)
    public User requireUser(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    @Transactional(readOnly = true)
    public Post requirePost(Long id) {
        return posts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Post not found."));
    }

    @Transactional(readOnly = true)
    public Page<Post> feed(Long userId, int page) {
        return posts.findFeed(userId, PageRequest.of(safePage(page), 20,
                Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Transactional(readOnly = true)
    public Page<Post> postsBy(Long userId, int page) {
        return posts.findByAuthorIdOrderByCreatedAtDesc(userId, PageRequest.of(safePage(page), 20));
    }

    @Transactional(readOnly = true)
    public List<Comment> commentsFor(Long postId) { return comments.findByPostIdOrderByCreatedAtAsc(postId); }

    @Transactional(readOnly = true)
    public List<User> search(String query) {
        String value = query == null ? "" : query.trim();
        if (value.length() > 50) {
            throw new IllegalArgumentException("Search terms must be 50 characters or fewer.");
        }
        return users.findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc(value);
    }

    public Post publish(Long userId, String content) {
        return posts.save(new Post(requireUser(userId), validatedText(content, 1000, "Post")));
    }

    public Comment comment(Long userId, Long postId, String content) {
        return comments.save(new Comment(requirePost(postId), requireUser(userId),
                validatedText(content, 500, "Comment")));
    }

    public boolean toggleFollow(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) throw new IllegalArgumentException("You cannot follow yourself.");
        requireUser(followerId);
        User followed = requireUser(followedId);
        Optional<Follow> existing = follows.findByFollowerIdAndFollowedId(followerId, followedId);
        if (existing.isPresent()) {
            follows.delete(existing.get());
            return false;
        }
        follows.save(new Follow(requireUser(followerId), followed));
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        return follows.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    public void resetPassword(Long userId, String password) {
        requireUser(userId).setPasswordHash(encoder.encode(validatedPassword(password)));
    }

    private String validatedText(String text, int max, String label) {
        if (text == null || text.trim().isEmpty()) throw new IllegalArgumentException(label + " cannot be empty.");
        String value = text.trim();
        if (value.length() > max) throw new IllegalArgumentException(label + " is too long.");
        return value;
    }

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizedUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private String validatedPassword(String password) {
        if (password == null || password.length() < 8 || !fitsBcrypt(password)) {
            throw new IllegalArgumentException("Password must contain 8-72 characters and fit within 72 bytes.");
        }
        return password;
    }

    private String validatedRecoveryPhrase(String phrase) {
        String normalized = normalizeAnswer(phrase);
        if (normalized.length() < 8 || !fitsBcrypt(normalized)) {
            throw new IllegalArgumentException(
                    "Recovery phrase must contain 8-72 characters and fit within 72 bytes.");
        }
        return normalized;
    }

    private boolean fitsBcrypt(String value) {
        return value != null && value.length() <= 72
                && value.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    private int safePage(int page) {
        return Math.max(0, Math.min(page, 500));
    }
}
