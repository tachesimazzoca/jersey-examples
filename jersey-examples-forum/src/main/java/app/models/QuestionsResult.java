package app.models;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;

@Entity
@SqlResultSetMapping(name = "QuestionsResult", classes = {
        @ConstructorResult(
                targetClass = QuestionsResult.class,
                columns = {
                        @ColumnResult(name = "questions.id"),
                        @ColumnResult(name = "questions.subject"),
                        @ColumnResult(name = "questions.body"),
                        @ColumnResult(name = "questions.posted_at"),
                        @ColumnResult(name = "accounts.id"),
                        @ColumnResult(name = "accounts.nickname") })
})
public class QuestionsResult {
    @Id
    private Long id;

    private String subject;

    private String body;

    private java.util.Date postedAt;

    private Long authorId;

    private String nickname;

    public QuestionsResult(
            BigInteger id,
            Clob subject,
            Clob body,
            java.util.Date postedAt,
            BigInteger authorId,
            String nickname) {
        if (id != null)
            this.id = id.longValue();
        try {
            this.subject = IOUtils.toString(subject.getCharacterStream());
            this.body = IOUtils.toString(body.getCharacterStream());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
        this.postedAt = postedAt;
        if (authorId != null)
            this.authorId = authorId.longValue();
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public java.util.Date getPostedAt() {
        return postedAt;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getNickname() {
        return nickname;
    }
}
