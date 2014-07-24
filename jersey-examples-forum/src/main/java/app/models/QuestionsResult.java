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
                        @ColumnResult(name = "questions_id"),
                        @ColumnResult(name = "questions_subject"),
                        @ColumnResult(name = "questions_posted_at"),
                        @ColumnResult(name = "accounts_id"),
                        @ColumnResult(name = "accounts_email") })
})
public class QuestionsResult {
    @Id
    private Long id;

    private String subject;

    private java.util.Date postedAt;

    private Long authorId;

    private String email;

    public QuestionsResult(
            BigInteger id,
            Clob subject,
            java.util.Date postedAt,
            BigInteger authorId,
            String email) {
        if (id != null)
            this.id = id.longValue();
        try {
            this.subject = IOUtils.toString(subject.getCharacterStream());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
        this.postedAt = postedAt;
        if (authorId != null)
            this.authorId = authorId.longValue();
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public java.util.Date getPostedAt() {
        return postedAt;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getEmail() {
        return email;
    }
}
