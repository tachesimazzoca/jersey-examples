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
                        @ColumnResult(name = "questions.status"),
                        @ColumnResult(name = "accounts.id"),
                        @ColumnResult(name = "accounts.nickname"),
                        @ColumnResult(name = "num_answers"),
                        @ColumnResult(name = "num_points") })
})
public class QuestionsResult {
    @Id
    private Long id;

    private String subject;

    private String body;

    private java.util.Date postedAt;

    private Question.Status status;

    private Long authorId;

    private String nickname;

    private Integer numAnswers;

    private Integer numPoints;

    public QuestionsResult(
            BigInteger id,
            Clob subject,
            Clob body,
            java.util.Date postedAt,
            byte status,
            BigInteger authorId,
            String nickname,
            BigInteger numAnswers,
            BigInteger numPoints) {
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
        this.status = Question.Status.fromValue((int) status);
        this.authorId = authorId.longValue();
        this.nickname = nickname;

        if (numAnswers != null)
            this.numAnswers = numAnswers.intValue();
        else
            this.numAnswers = 0;

        if (numPoints != null)
            this.numPoints = numPoints.intValue();
        else
            this.numPoints = 0;
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

    public Question.Status getStatus() {
        return this.status;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getNumAnswers() {
        return numAnswers;
    }

    public Integer getNumPoints() {
        return numPoints;
    }
}
