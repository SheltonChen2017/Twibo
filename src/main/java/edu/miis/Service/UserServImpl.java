package edu.miis.Service;

import edu.miis.Dao.ArticlesRepository;
import edu.miis.Dao.CommentRepository;
import edu.miis.Dao.RelationshipRepository;
import edu.miis.Dao.RepostRepository;
import edu.miis.Dao.UserRepository;
import edu.miis.DataTransferPojo.ArticleTransferPojo;
import edu.miis.DataTransferPojo.CommentTransferPojo;
import edu.miis.DataTransferPojo.RegistrationForm;
import edu.miis.DataTransferPojo.UserTransferPojo;
import edu.miis.Entities.Article;
import edu.miis.Entities.Comment;
import edu.miis.Entities.Relationship;
import edu.miis.Entities.Repost;
import edu.miis.Entities.SecurityQuestion;
import edu.miis.Entities.UserBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class UserServImpl implements IUserServ {

    private final UserRepository userRepository;
    private final ArticlesRepository articleRepository;
    private final CommentRepository commentRepository;
    private final RelationshipRepository relationshipRepository;
    private final RepostRepository repostRepository;
    private final PasswordEncoder passwordEncoder;
    private final int feedPageSize;
    private final String dummyAnswerHash;

    public UserServImpl(
            UserRepository userRepository,
            ArticlesRepository articleRepository,
            CommentRepository commentRepository,
            RelationshipRepository relationshipRepository,
            RepostRepository repostRepository,
            PasswordEncoder passwordEncoder,
            @Value("${twibo.feed.page-size:30}") int feedPageSize
    ) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.relationshipRepository = relationshipRepository;
        this.repostRepository = repostRepository;
        this.passwordEncoder = passwordEncoder;
        this.feedPageSize = Math.max(1, Math.min(feedPageSize, 100));
        this.dummyAnswerHash = passwordEncoder.encode("twibo-invalid-recovery-answer");
    }

    @Override
    public UserBean register(RegistrationForm form) {
        String username = normalizeUsername(form.getUsername());
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("That username is already in use.");
        }

        UserBean user = new UserBean();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setBirthday(form.getBirthday());

        SecurityQuestion securityQuestion = new SecurityQuestion();
        securityQuestion.setQuestion(form.getSecurityQuestion().trim());
        securityQuestion.setAnswer(passwordEncoder.encode(form.getSecurityAnswer()));
        user.addSecurityQuestion(securityQuestion);

        return userRepository.saveAndFlush(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserBean> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(normalizeUsername(username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserBean requireUser(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserBean requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Article requireArticle(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @Override
    public Article publish(String username, String content) {
        String normalizedContent = normalizeContent(content, 5000, "Post");
        UserBean author = requireUser(username);
        Article article = new Article();
        article.setContent(normalizedContent);
        article.setAuthor(author);
        return articleRepository.save(article);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleTransferPojo> loadFeed(String username, int page) {
        UserBean user = requireUser(username);
        List<Long> authorIds = new ArrayList<>(relationshipRepository.findFollowedIds(user.getId()));
        authorIds.add(user.getId());
        return articleRepository.findByAuthorIdIn(authorIds, articlePage(page))
                .map(ArticleTransferPojo::from)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleTransferPojo> loadIndividualPage(Long userId, int page) {
        requireUser(userId);
        return articleRepository.findByAuthorId(userId, articlePage(page))
                .map(ArticleTransferPojo::from)
                .getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentTransferPojo> loadComments(Long articleId) {
        requireArticle(articleId);
        return commentRepository.findByArticleIdOrderByIdAsc(articleId).stream()
                .map(CommentTransferPojo::from)
                .toList();
    }

    @Override
    public void addComment(String username, Long articleId, String content) {
        Comment comment = new Comment();
        comment.setAuthor(requireUser(username));
        comment.setArticle(requireArticle(articleId));
        comment.setContent(normalizeContent(content, 5000, "Comment"));
        commentRepository.save(comment);
    }

    @Override
    public boolean toggleFollow(String username, Long followedUserId) {
        UserBean owner = requireUser(username);
        requireUser(followedUserId);
        if (owner.getId().equals(followedUserId)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        Optional<Relationship> relationship =
                relationshipRepository.findByUserIdAndFid(owner.getId(), followedUserId);
        if (relationship.isPresent()) {
            relationshipRepository.delete(relationship.get());
            return false;
        }
        relationshipRepository.save(new Relationship(followedUserId, owner));
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(String username, Long followedUserId) {
        UserBean owner = requireUser(username);
        return relationshipRepository.existsByUserIdAndFid(owner.getId(), followedUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTransferPojo> searchByName(String name) {
        String query = name == null ? "" : name.trim();
        if (query.isEmpty()) {
            return List.of();
        }
        return userRepository.searchByUsername(query, PageRequest.of(0, 50)).stream()
                .map(UserTransferPojo::from)
                .toList();
    }

    @Override
    public boolean repost(String username, Long articleId, String comment) {
        UserBean user = requireUser(username);
        Article article = requireArticle(articleId);
        if (repostRepository.existsByRefIdAndForwarderId(articleId, user.getId())) {
            return false;
        }

        Repost repost = new Repost();
        repost.setForwarder(user);
        repost.setRef(article);
        repost.setComment(comment == null ? "" : normalizeContent(comment, 500, "Repost comment"));
        repostRepository.save(repost);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> loadSecurityQuestions(String username) {
        return userRepository.findWithSecurityQuestions(normalizeUsername(username))
                .map(user -> user.getSecurityQuestions().stream()
                        .map(SecurityQuestion::getQuestion)
                        .sorted()
                        .toList())
                .orElse(List.of());
    }

    @Override
    public boolean verifySecurityAnswer(String username, String question, String answer) {
        Optional<UserBean> optionalUser = userRepository.findWithSecurityQuestions(normalizeUsername(username));
        if (optionalUser.isEmpty() || question == null || answer == null) {
            if (answer != null) {
                passwordEncoder.matches(answer, dummyAnswerHash);
            }
            return false;
        }

        Optional<SecurityQuestion> match = optionalUser.get().getSecurityQuestions().stream()
                .filter(item -> item.getQuestion().equals(question))
                .filter(item -> passwordEncoder.matches(answer, item.getAnswer()))
                .findFirst();
        match.filter(item -> passwordEncoder.upgradeEncoding(item.getAnswer()))
                .ifPresent(item -> item.setAnswer(passwordEncoder.encode(answer)));
        return match.isPresent();
    }

    @Override
    public void resetPassword(String username, String password) {
        if (password == null || password.length() < 10 || password.length() > 72) {
            throw new IllegalArgumentException("Password must contain between 10 and 72 characters.");
        }
        UserBean user = requireUser(username);
        user.setPassword(passwordEncoder.encode(password));
    }

    private PageRequest articlePage(int page) {
        int safePage = Math.max(page, 0);
        return PageRequest.of(
                safePage,
                feedPageSize,
                Sort.by(Sort.Order.desc("insertTime"), Sort.Order.desc("id"))
        );
    }

    private static String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeContent(String value, int maxLength, String label) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(label + " is too long.");
        }
        return normalized;
    }
}
