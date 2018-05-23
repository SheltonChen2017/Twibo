package edu.miis.Entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
//import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.stereotype.Component;

@Component
@Entity(name = "SecurityQA")
public class SecurityQuestion implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserBean ownerr;
	@Column(length = 50, nullable = false, unique = false)
	private String question;
	@Column(length = 50, nullable = false, unique = false)
	private String answer;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SecurityQuestion() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SecurityQuestion(Long id, UserBean ownerr, String question, String answer) {
		super();
		this.id = id;
		this.ownerr = ownerr;
		this.question = question;
		this.answer = answer;
	}

	public UserBean getOwnerr() {
		return ownerr;
	}

	public void setOwnerr(UserBean ownerr) {
		this.ownerr = ownerr;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SecurityQuestion that = (SecurityQuestion) o;
		return Objects.equals(question, that.question) &&
				Objects.equals(answer, that.answer);
	}

	@Override
	public int hashCode() {

		return Objects.hash(question, answer);
	}
}
