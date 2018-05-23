package edu.miis.Service;

import edu.miis.DataTransferPojo.ArticleTransferPojo;
import edu.miis.Entities.Article;
import edu.miis.Entities.Comment;
import edu.miis.Entities.Repost;
import edu.miis.Entities.UserBean;
import org.hibernate.Session;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface IUserServ {

boolean Login(UserBean user);
boolean Register(UserBean user);
boolean verifyUsername(String username);
String loadUsername(String username);
boolean verifyQA(String username, String question, String answer);
UserBean selectByUsername(String username);
UserBean updateUser(UserBean user);
    List<UserBean> selectByExample(UserBean user);
    List<Article> loadAllArticles();
    Set<Article> loadUserArticles(Long UserID);
    UserBean loadUser(Long UserID);
    Session getSession();

    void saveComent(Long id, String commentContent, UserBean user);
  List<Article> loadArticles(UserBean user);

    List<Article> queryByTime(Long id,Date lastLog);
    List<Comment> queryCommentByTime(Long aid,Long entry);
    Article loadArticleById(Long aid);
    Article loadOneArticle(Long articleId);

    List<ArticleTransferPojo> loadIndividualPage(Long userId, Date currentDate);

    List<UserBean> searchByName(String name);

    void saveRepost(Repost repost);


//    String popQuestions(String username);
}
