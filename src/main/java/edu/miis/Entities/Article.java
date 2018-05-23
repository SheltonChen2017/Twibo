package edu.miis.Entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.*;
//import javax.persistence.OneToOne;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.persistence.Entity;

//@Component
@Entity
@Table(name="Article")
public class Article implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = true, unique = false, length = 100)
	private String title;
	@Column(nullable = false, unique = false, length = 5000)
	private String content;
	@ManyToOne(optional = false,cascade=CascadeType.ALL)
	@JoinColumn(name = "author_id")
	private UserBean author;
	@ManyToOne
	@JoinColumn(name = "theme_id")
	private Theme theme;
	@OneToMany(mappedBy = "article",fetch= FetchType.EAGER,cascade=CascadeType.ALL)
	private Set<Comment> comments;
	@Column
//	@DateTimeFormat(pattern="yyyy-MM-dd")
	@CreatedDate
	private Date insertTime = new Date();

	@OneToMany(cascade = CascadeType.ALL,mappedBy = "ref")
	private Set<Repost> reposts;

	public Set<Repost> getReposts() {
		return reposts;
	}

	public void setReposts(Set<Repost> reposts) {
		this.reposts = reposts;
	}



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public UserBean getAuthor() {
		return author;
	}

	public void setAuthor(UserBean author) {
		this.author = author;
	}

	public Theme getTheme() {
		return theme;
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public Set<Comment> getComments() {
		return comments;
	}

	public void setComments(Set<Comment> comments) {
		this.comments = comments;
	}

	public Article(String title, String content, UserBean author, Theme theme, Set<Comment> comments) {
		this.title = title;
		this.content = content;
		this.author = author;
		this.theme = theme;
		this.comments = comments;
	}

	public Article() {
	}
}
