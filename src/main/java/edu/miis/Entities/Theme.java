package edu.miis.Entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.springframework.stereotype.Component;
@Component
@Entity(name="Themee")
public class Theme implements Serializable{

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="ThemeName",nullable=false,unique=true,length=35)
	private String name;
	
	@OneToMany(mappedBy="theme")
	private Set<Article> article;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Set<Article> getArticle() {
		return article;
	}
	public void setArticle(Set<Article> article) {
		this.article = article;
	}
	public Theme(Long id, Set<Article> article) {
		super();
		this.id = id;
		this.article = article;
	}
	public Theme() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
