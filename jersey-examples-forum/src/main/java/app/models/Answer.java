package app.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "question_id")
    private Long questionId = 0L;

    @Column(name = "author_id")
    private Long authorId = 0L;

    private String body = "";

    @Column(name = "posted_at")
    private java.util.Date postedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
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
