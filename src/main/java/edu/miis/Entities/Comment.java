package edu.miis.Entities;

import java.io.Serializable;

import javax.persistence.*;
//import javax.persistence.OneToOne;

import org.springframework.stereotype.Component;

//@Component
@Entity(name = "commentt")
public class Comment implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = false, length = 5000)
	private String content;
	@ManyToOne
	private Article article;
	@ManyToOne
	@JoinColumn(name = "userbean_id")
	private UserBean author;

	@OneToOne(mappedBy = "comment",cascade=CascadeType.ALL)
	@JoinColumn(name="notification")
	private PostNotification notification;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Article getArticle() {
		return article;
	}
	public void setArticle(Article article) {
		this.article = article;
	}
	public UserBean getAuthor() {
		return author;
	}
	public void setAuthor(UserBean author) {
		this.author = author;
	}
	public Comment(Long id, Article article, UserBean author) {
		super();
		this.id = id;
		this.article = article;
		this.author = author;
	}
	public Comment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PostNotification getNotification() {
		return notification;
	}

	public void setNotification(PostNotification notification) {
		this.notification = notification;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
