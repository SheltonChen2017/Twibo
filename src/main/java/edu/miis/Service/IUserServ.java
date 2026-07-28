package edu.miis.Service;

import edu.miis.DataTransferPojo.ArticleTransferPojo;
import edu.miis.DataTransferPojo.CommentTransferPojo;
import edu.miis.DataTransferPojo.RegistrationForm;
import edu.miis.DataTransferPojo.UserTransferPojo;
import edu.miis.Entities.Article;
import edu.miis.Entities.UserBean;

import java.util.List;
import java.util.Optional;

public interface IUserServ {

    UserBean register(RegistrationForm form);

    Optional<UserBean> findByUsername(String username);

    UserBean requireUser(String username);

    UserBean requireUser(Long id);

    Article requireArticle(Long id);

    Article publish(String username, String content);

    List<ArticleTransferPojo> loadFeed(String username, int page);

    List<ArticleTransferPojo> loadIndividualPage(Long userId, int page);

    List<CommentTransferPojo> loadComments(Long articleId);

    void addComment(String username, Long articleId, String content);

    boolean toggleFollow(String username, Long followedUserId);

    boolean isFollowing(String username, Long followedUserId);

    List<UserTransferPojo> searchByName(String name);

    boolean repost(String username, Long articleId, String comment);

    List<String> loadSecurityQuestions(String username);

    boolean verifySecurityAnswer(String username, String question, String answer);

    void resetPassword(String username, String password);
}
