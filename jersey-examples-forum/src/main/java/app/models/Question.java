package app.models;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "author_id")
    private Long authorId = 0L;

    private String subject = "";

    private String body = "";

    @Column(name = "posted_at")
    private java.util.Date postedAt;

    @Convert(converter = Question.StatusConverter.class)
    private Status status = Status.PUBLISHED;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        PUBLISHED(0, "Published"), DELETED(1, "Deleted"), DRAFT(2, "Draft");

        private int value;
        private String label;

        private Status(int value, String label) {
            this.value = value;
            this.label = label;
        }

        public int getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }

        public static Status fromValue(int v) {
            for (Status s : Status.values()) {
                if (s.getValue() == v) {
                    return s;
                }
            }
            throw new IllegalArgumentException("unknown value: " + v);
        }

        public static Status fromValue(String str) {
            return fromValue(Integer.valueOf(str));
        }
    }

    public static class StatusConverter implements
            AttributeConverter<Status, Integer> {
        @Override
        public Integer convertToDatabaseColumn(Status status) {
            return status.getValue();
        }

        @Override
        public Status convertToEntityAttribute(Integer value) {
            return Status.fromValue(value);
        }
    }

    public boolean isSameAuthor(Account account) {
        if (getAuthorId() == 0)
            return false;
        if (getAuthorId() != account.getId())
            return false;
        return true;
    }
}
