package edu.miis.Service;

import edu.miis.DataTransferPojo.ArticleTransferPojo;
import edu.miis.DataTransferPojo.RegistrationForm;
import edu.miis.Entities.Article;
import edu.miis.Entities.UserBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServImplIntegrationTest {

    @Autowired
    private IUserServ userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registrationHashesSecretsAndRejectsDuplicateUsernames() {
        UserBean user = userService.register(registration("Alice"));

        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getPassword()).doesNotContain("correct horse");
        assertThat(passwordEncoder.matches("correct horse battery staple", user.getPassword())).isTrue();
        assertThat(user.getSecurityQuestions()).singleElement()
                .satisfies(question -> assertThat(question.getAnswer()).doesNotContain("blue"));

        assertThatThrownBy(() -> userService.register(registration("ALICE")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already");
    }

    @Test
    void feedFollowCommentsAndRepostsWorkTogether() {
        UserBean alice = userService.register(registration("alice"));
        UserBean bob = userService.register(registration("bob"));
        Article post = userService.publish("bob", "Hello from Bob");

        assertThat(userService.loadFeed("alice", 0)).isEmpty();
        assertThat(userService.toggleFollow("alice", bob.getId())).isTrue();

        List<ArticleTransferPojo> feed = userService.loadFeed("alice", 0);
        assertThat(feed).extracting(ArticleTransferPojo::content).containsExactly("Hello from Bob");

        userService.addComment("alice", post.getId(), "Welcome!");
        assertThat(userService.loadComments(post.getId()))
                .singleElement()
                .satisfies(comment -> {
                    assertThat(comment.content()).isEqualTo("Welcome!");
                    assertThat(comment.authorName()).isEqualTo(alice.getUsername());
                });

        assertThat(userService.repost("alice", post.getId(), "Worth sharing")).isTrue();
        assertThat(userService.repost("alice", post.getId(), "Duplicate")).isFalse();
        assertThat(userService.toggleFollow("alice", bob.getId())).isFalse();
    }

    @Test
    void passwordRecoveryVerifiesHashedAnswerAndChangesPassword() {
        userService.register(registration("recover-me"));

        assertThat(userService.loadSecurityQuestions("recover-me")).containsExactly("Favourite colour?");
        assertThat(userService.verifySecurityAnswer("recover-me", "Favourite colour?", "wrong")).isFalse();
        assertThat(userService.verifySecurityAnswer("recover-me", "Favourite colour?", "blue")).isTrue();

        userService.resetPassword("recover-me", "a completely new password");
        assertThat(passwordEncoder.matches(
                "a completely new password",
                userService.requireUser("recover-me").getPassword()
        )).isTrue();
    }

    private static RegistrationForm registration(String username) {
        RegistrationForm form = new RegistrationForm();
        form.setUsername(username);
        form.setPassword("correct horse battery staple");
        form.setBirthday(LocalDate.of(1990, 1, 1));
        form.setSecurityQuestion("Favourite colour?");
        form.setSecurityAnswer("blue");
        form.setCaptcha("unused-in-service");
        return form;
    }
}
