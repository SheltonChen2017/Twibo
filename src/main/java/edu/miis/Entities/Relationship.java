package edu.miis.Entities;

import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Component
public class Relationship implements Serializable {

   /*
   *
   * 关系设计：
   * 该表为 关注表。
   * 
   * */

@Id
@GeneratedValue(strategy= GenerationType.IDENTITY)
private Long id;
@Column(nullable=false,unique=false,name="followed")
private Long fid;
@ManyToOne(optional=false)
@JoinColumn(name="ownerID")
private UserBean user;

    public Relationship(Long fid,UserBean user) {
        this.fid = fid;
        this.user=user;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Relationship() {
    }

    public Long getFid() {
        return fid;
    }

    public void setFid(Long fid) {
        this.fid = fid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relationship)) return false;
        Relationship that = (Relationship) o;
        return Objects.equals(getFid(), that.getFid()) &&
                Objects.equals(getUser(), that.getUser());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getFid(), getUser());
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

}
