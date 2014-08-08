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
@SqlResultSetMapping(name = "AnswersResult", classes = {
        @ConstructorResult(
                targetClass = AnswersResult.class,
                columns = {
                        @ColumnResult(name = "answers.id"),
                        @ColumnResult(name = "answers.question_id"),
                        @ColumnResult(name = "answers.body"),
                        @ColumnResult(name = "answers.posted_at"),
                        @ColumnResult(name = "accounts.id"),
                        @ColumnResult(name = "accounts.nickname") })
})
public class AnswersResult {
    @Id
    private Long id;

    private Long questionId;

    private String body;

    private java.util.Date postedAt;

    private Long authorId;

    private String nickname;

    public AnswersResult(
            BigInteger id,
            BigInteger questionId,
            Clob body,
            java.util.Date postedAt,
            BigInteger authorId,
            String nickname) {
        this.id = id.longValue();
        this.questionId = questionId.longValue();
        try {
            this.body = IOUtils.toString(body.getCharacterStream());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
        this.postedAt = postedAt;
        this.authorId = authorId.longValue();
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
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
