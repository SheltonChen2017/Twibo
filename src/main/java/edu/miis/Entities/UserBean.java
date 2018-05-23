package edu.miis.Entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

@Entity(name = "UserInfo")
@Component
public class UserBean implements Serializable {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(length = 35, unique = true, nullable = false)
	@NotNull(message="Username can't be left empty!")
	@Size(min=8,max=20,message="Username must be longer than 8 and shorter than 20 characters")
	private String username;
	@NotNull(message="Password can't be left empty!")
	@Size(min=8,max=20,message="Password must be longer than 8 and shorter than 20 characters")
	@Column(length = 35, unique = false, nullable = false)
	private String password;
	@Column(name = "DateOfBirth")
	@NotNull(message="Date of Birth is used for password retrieval thus cannot be left alone")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Temporal(TemporalType.DATE)
	private Date birthday;
	@Column(length = 150, unique = true, nullable = true)
	private String profilePhoto;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "author",fetch= FetchType.EAGER)
	private Set<Article> articles = new HashSet<Article>();
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "author",fetch= FetchType.EAGER)
	private Set<Comment> comments = new HashSet<Comment>();
	@OneToMany(cascade=CascadeType.ALL,mappedBy="user",orphanRemoval=true,fetch= FetchType.EAGER)
	private Set<Relationship> relationships= new HashSet<Relationship>();
	@OneToMany(cascade=CascadeType.ALL,mappedBy="sender",fetch=FetchType.EAGER)
	private Set<PostNotification> notifications=new HashSet<PostNotification>();
	@LastModifiedDate
	@Column
	private Date lastLoginTime = new Date();

	@OneToMany(mappedBy="forwarder",cascade = CascadeType.ALL,fetch = FetchType.EAGER)
	private Set<Repost> reposts;

	public Set<Repost> getReposts() {
		return reposts;
	}

	public void setReposts(Set<Repost> reposts) {
		this.reposts = reposts;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public Set<Relationship> getRelationships() {
		return relationships;
	}

	public Set<PostNotification> getNotifications() {
		return notifications;
	}

	public void setNotifications(Set<PostNotification> notifications) {
		this.notifications = notifications;
	}

	public void setRelationships(Set<Relationship> relationships) {
		this.relationships = relationships;
	}

	@OneToMany(mappedBy = "ownerr",cascade={CascadeType.ALL})
	private Set<SecurityQuestion> securityquestions=new HashSet<SecurityQuestion>();


	public void addQA(SecurityQuestion sq){

		if(sq!=null) {
			securityquestions.add(sq);
		}
	}

	public UserBean(Long id, String username, String password, Date birthday, String profilePhoto,
			Set<Article> articles, Set<Comment> comments,
			Set<SecurityQuestion> securityquestions) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.birthday = birthday;
		this.profilePhoto = profilePhoto;
		this.articles = articles;
		this.comments = comments;
		this.securityquestions = securityquestions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof UserBean)) return false;
		UserBean userBean = (UserBean) o;
		return Objects.equals(getUsername(), userBean.getUsername()) &&
				Objects.equals(getPassword(), userBean.getPassword());
	}

	@Override
	public int hashCode() {

		return Objects.hash(getUsername(), getPassword());
	}

	public UserBean() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getProfilePhoto() {
		return profilePhoto;
	}

	public void setProfilePhoto(String profilePhoto) {
		this.profilePhoto = profilePhoto;
	}

	public Set<Article> getArticles() {
		return articles;
	}

	public void setArticles(Set<Article> articles) {
		this.articles = articles;
	}

	public Set<Comment> getComments() {
		return comments;
	}

	public void setComments(Set<Comment> comments) {
		this.comments = comments;
	}


	public Set<SecurityQuestion> getSecurityquestions() {
		return securityquestions;
	}

	public void setSecurityquestions(Set<SecurityQuestion> securityquestions) {
		this.securityquestions = securityquestions;
	}

}
