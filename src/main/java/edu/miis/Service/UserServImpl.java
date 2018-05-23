package edu.miis.Service;



import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;


import edu.miis.Dao.ArticlesRepository;
import edu.miis.Dao.RepostRepository;
import edu.miis.Dao.UserRepository;
import edu.miis.DataTransferPojo.ArticleTransferPojo;
import edu.miis.Entities.*;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("userServ")
@Transactional(propagation=Propagation.REQUIRED, readOnly=false,rollbackFor={Exception.class, RuntimeException.class})
public class UserServImpl implements IUserServ {
//	@Resource(name="userDao")
	@Autowired
	private UserRepository userDao;
//	@Resource(name="articleDao")
	@Autowired
	private ArticlesRepository articleDao;
@Autowired
	private RepostRepository repostRepository;
	@Override
	public boolean Login(UserBean user) {

	List<UserBean> list = userDao.selectAll();
//userDao.
//		Iterable<UserBean> all = userDao.findAll();

		for(UserBean userbean:list){

		if(user.equals(userbean)){
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			String format = sdf.format(new Date());
			Date date = new Date();
		userbean.setLastLoginTime(date);
		userDao.save(userbean);
			return true;
		}
	}
		return false;
	}

	@Override
	public boolean Register(UserBean user) {

		userDao.save(user);

		return false;
	}

	@Override
	public boolean verifyUsername(String username) {
//		System.out.println(userDao.toString());
		List<UserBean> ub = userDao.usernameVerify(username);

		if(!ub.isEmpty()){
			return false;
		}

		return true;
	}

	@Override
	public String loadUsername(String username) {

//		String state=userDao.popQuestions(username);
//		return state;
		return null;
	}

	@Override
	public boolean verifyQA(String username, String question, String answer) {
		List<UserBean> list = userDao.selectByUsername(username);
		UserBean user = list.get(0);
		Set<SecurityQuestion> sqs = user.getSecurityquestions();
		System.out.println("smiling from UserServImpl.java"+user.getUsername());
		SecurityQuestion sq = new SecurityQuestion();
		sq.setAnswer(answer);
		sq.setQuestion(question);
		boolean flag = sqs.contains(sq);
		return flag;

	}

	public UserBean selectByUsername(String username){
		List<UserBean> userBeans = userDao.selectByUsername(username);
		UserBean user = userBeans.get(0);

		return user;
	}

	@Override
	public UserBean updateUser(UserBean user) {

		UserBean merge = userDao.save(user);
//		userDao.
		return merge;
	}

	public List<UserBean> selectByExample(UserBean user){
		List<UserBean> list = userDao.selectAll();
return list;

	}

	@Override
	public List<Article> loadAllArticles() {
		List<Article> list = articleDao.loadAll();
		return list;
	}



	@Override
	public Set<Article> loadUserArticles(Long userID) {

		List<UserBean> load = userDao.load(userID);
		UserBean ub = load.get(0);
		Set<Article> personalArticles = ub.getArticles();

		return personalArticles;


	}

	@Override
	public UserBean loadUser(Long UserID) {
		List<UserBean> load = userDao.load(UserID);
		UserBean individual = load.get(0);
		return individual;

	}

	@Override
	public Session getSession() {
		return null;
	}

	@Override
	public void saveComent(Long articleID, String commentContent, UserBean user) {

		List<Article> list = articleDao.loadOne(articleID);
		Article article = list.get(0);
		Comment comment = new Comment();
		comment.setArticle(article);
		comment.setContent(commentContent);
		comment.setAuthor(user);
//		article.getComments().add(comment);
		PostNotification pn = new PostNotification();
		pn.setComment(comment);
		pn.setSender(user);
		user.getNotifications().add(pn);
		comment.setNotification(pn);
		user.getComments().add(comment);

		articleDao.save(article);



	}

	@Override
	public List<Article> loadArticles(UserBean user) {
		ArrayList<Article> articles = new ArrayList<>();
		UserBean ub = this.selectByUsername(user.getUsername());
		Set<Relationship> rls = ub.getRelationships();
		for(Relationship rlt:rls){
			Long fid = rlt.getFid();
			Set<Article> userArticles = this.loadUserArticles(fid);
			for(Article a:userArticles){
				articles.add(a);
			}
		}

		 Set<Article> a2 = ub.getArticles();
			for(Article a:a2){

				articles.add(a);

			}

		Collections.sort(articles,new Comparator<Article>(){
			@Override
			public int compare(Article a1, Article a2) {
				if(a1.getId()>a2.getId()){
					return 1;
				}else if(a1.getId()==a2.getId()){
					return 0;

				} else {
					return -1;

				}


			}
		});

		return articles;
	}

	@Override
	public List<Article> queryByTime(Long id, Date lastLog) {

		System.out.println("dfdfdfdfdfdfdfdfdfdfdfdfdfdfdfd!!!!!!!!!!!!!!!!!!!!!!!");
		List<Article> list=articleDao.queryByTime(id,lastLog);
System.out.println("dfdfdfdfdfdfdfdfdfdfdfdfdfdfdfd!!!!!!!!!!!!!!!!!!!!!!!");


//		return list;
		return list;
	}

	@Override
	public List<Comment> queryCommentByTime(Long aid, Long entry) {
//		return null;

		List<Comment> comments = articleDao.queryCommentByTime(aid);
		return comments;
	}

	@Override
	public Article loadArticleById(Long aid) {
		List<Article> list = articleDao.loadOne(aid);

		return list.get(0);
	}

	@Override
	public Article loadOneArticle(Long articleId) {

		List<Article> list = articleDao.queryById(articleId);

		if(!list.isEmpty()){
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<ArticleTransferPojo> loadIndividualPage(Long userId, Date currentDate) {
		List<Article> articles = articleDao.queryByTime(userId,currentDate);

//		String authorName;
//		Long authorId;
//		String content;
//		Long articleId;
//		Date articleDate;
		ArrayList<ArticleTransferPojo> atps = new ArrayList<>();
		for (Article a:articles){
			ArticleTransferPojo atp = new ArticleTransferPojo(a.getAuthor().getUsername(), a.getAuthor().getId(), a.getContent(), a.getId(), a.getInsertTime());
			atps.add(atp);

		}

		// 按照id 排序

		Collections.sort(atps, new Comparator<ArticleTransferPojo>() {
			@Override
			public int compare(ArticleTransferPojo a1, ArticleTransferPojo a2) {
				if(a1.getArticleId()<a2.getArticleId()){
					return 1;
				} else if(a1.getArticleId()==a2.getArticleId()){
					return 0;
				}else{
					return -1;
				}
			}
		});


		return atps;
	}

	@Override
	public List<UserBean> searchByName(String name) {
		List<UserBean> users=userDao.searchByName(name);
		return users;
	}

	@Override
	public void saveRepost(Repost repost) {

		repostRepository.save(repost);

	}


}
