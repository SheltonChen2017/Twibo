package edu.miis.Entities;

import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;

//@Component
@Entity
public class PostNotification implements Serializable {
@Id
@GeneratedValue(strategy= GenerationType.IDENTITY)
 private  Long id;

/*
* 每发出一条评论，就势必产生一条通知。
* 通知有四方 发送方 接收方 对应的评论 和已读状态
* 与发送方：多对一
* 与接收方：多对一
*  与评论： 一对一
*  已读状态：true/false
* */

    public PostNotification(Comment comment, UserBean sender, boolean status) {
        this.comment = comment;
        this.sender = sender;
        this.status = status;
    }

    @OneToOne
@JoinColumn(name="comment")
    private Comment comment;
@ManyToOne
@JoinColumn(name="sender")
   private UserBean sender;

@Column(name="readStatus")
private boolean status=false;


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public PostNotification(Comment comment, UserBean sender) {
        this.comment = comment;
        this.sender = sender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public UserBean getSender() {
        return sender;
    }

    public void setSender(UserBean sender) {
        this.sender = sender;
    }

    public PostNotification() {

    }
}
