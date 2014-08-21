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
                        @ColumnResult(name = "answers.status"),
                        @ColumnResult(name = "accounts.id"),
                        @ColumnResult(name = "accounts.nickname"),
                        @ColumnResult(name = "sum_points"),
                        @ColumnResult(name = "positive_points"),
                        @ColumnResult(name = "negative_points") })
})
public class AnswersResult {
    @Id
    private Long id;

    private Long questionId;

    private String body;

    private java.util.Date postedAt;

    private Answer.Status status;

    private Long authorId;

    private String nickname;

    private Integer sumPoints = 0;

    private Integer positivePoints = 0;

    private Integer negativePoints = 0;

    public AnswersResult(
            BigInteger id,
            BigInteger questionId,
            Clob body,
            java.util.Date postedAt,
            byte status,
            BigInteger authorId,
            String nickname,
            BigInteger sumPoints,
            BigInteger positivePoints,
            BigInteger negativePoints) {

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
        this.status = Answer.Status.fromValue((int) status);
        this.authorId = authorId.longValue();
        this.nickname = nickname;
        if (sumPoints != null)
            this.sumPoints = sumPoints.intValue();
        if (positivePoints != null)
            this.positivePoints = positivePoints.intValue();
        if (negativePoints != null)
            this.negativePoints = negativePoints.intValue();
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

    public Answer.Status getStatus() {
        return status;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getSumPoints() {
        return sumPoints;
    }

    public Integer getPositivePoints() {
        return positivePoints;
    }

    public Integer getNegativePoints() {
        return negativePoints;
    }
}
