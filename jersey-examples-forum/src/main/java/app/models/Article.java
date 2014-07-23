package app.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(name = "author_id")
    private Long authorId = 0L;

    private String subject = "";

    private String body = "";

    @Column(name = "posted_at")
    private java.util.Date postedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public java.util.Date getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(java.util.Date postedAt) {
        this.postedAt = postedAt;
    }
}
