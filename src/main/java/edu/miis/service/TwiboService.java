package edu.miis.service;

import edu.miis.domain.*;
import edu.miis.repository.*;
import edu.miis.web.SignupForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class TwiboService {
    private final UserRepository users;
    private final PostRepository posts;
    private final CommentRepository comments;
    private final FollowRepository follows;
    private final PasswordEncoder encoder;

    public TwiboService(UserRepository users, PostRepository posts, CommentRepository comments,
                        FollowRepository follows, PasswordEncoder encoder) {
        this.users = users;
        this.posts = posts;
        this.comments = comments;
        this.follows = follows;
        this.encoder = encoder;
    }

    public User register(SignupForm form) {
        String username = form.getUsername().trim();
        if (users.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("That username is already taken.");
        }
        return users.save(new User(username, encoder.encode(form.getPassword()), form.getBirthday(),
                form.getSecurityQuestion().trim(), encoder.encode(normalizeAnswer(form.getSecurityAnswer()))));
    }

    @Transactional(readOnly = true)
    public Optional<User> findUser(String username) {
        return users.findByUsernameIgnoreCase(username.trim());
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
    public List<Post> feed(Long userId) { return posts.findFeed(userId); }

    @Transactional(readOnly = true)
    public List<Post> postsBy(Long userId) { return posts.findByAuthorIdOrderByCreatedAtDesc(userId); }

    @Transactional(readOnly = true)
    public List<Comment> commentsFor(Long postId) { return comments.findByPostIdOrderByCreatedAtAsc(postId); }

    @Transactional(readOnly = true)
    public List<User> search(String query) {
        return users.findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc(query.trim());
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

    @Transactional(readOnly = true)
    public boolean verifyRecoveryAnswer(User user, String answer) {
        return encoder.matches(normalizeAnswer(answer), user.getSecurityAnswerHash());
    }

    public void resetPassword(Long userId, String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("Password must contain 8–72 characters.");
        }
        requireUser(userId).setPasswordHash(encoder.encode(password));
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
}
