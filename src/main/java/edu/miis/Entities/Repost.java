package edu.miis.Entities;

import javax.annotation.Generated;
import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
//import javax.xml.bind.annotation.XmlIDREF;

@Entity
public class Repost {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

@ManyToOne(cascade=CascadeType.ALL)
    private Article ref;
@ManyToOne(cascade=CascadeType.ALL)
    private UserBean forwarder;
@Column
private String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Repost(Article ref, UserBean forwarder) {
        this.ref = ref;
        this.forwarder = forwarder;
    }

    public Repost() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Repost)) return false;
        Repost repost = (Repost) o;
        return Objects.equals(getId(), repost.getId()) &&
                Objects.equals(getRef(), repost.getRef()) &&
                Objects.equals(getForwarder(), repost.getForwarder());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId(), getRef(), getForwarder());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Article getRef() {
        return ref;
    }

    public void setRef(Article ref) {
        this.ref = ref;
    }

    public UserBean getForwarder() {
        return forwarder;
    }

    public void setForwarder(UserBean forwarder) {
        this.forwarder = forwarder;
    }
}
