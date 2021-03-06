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
                        @ColumnResult(name = "sum_points"),
                        @ColumnResult(name = "positive_points"),
                        @ColumnResult(name = "negative_points") })
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

    private Integer numAnswers = 0;

    private Integer sumPoints = 0;

    private Integer positivePoints = 0;

    private Integer negativePoints = 0;

    public QuestionsResult(
            BigInteger id,
            Clob subject,
            Clob body,
            java.util.Date postedAt,
            byte status,
            BigInteger authorId,
            String nickname,
            BigInteger numAnswers,
            BigInteger sumPoints,
            BigInteger positivePoints,
            BigInteger negativePoints) {

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

    public Integer getSumPoints() {
        return sumPoints;
    }

    public Integer getPositivePoints() {
        return positivePoints;
    }

    public Integer getNegativePoints() {
        return negativePoints;
    }

    public enum OrderBy {
        POSTED_AT_DESC("posted_at_desc",
                "questions.posted_at DESC, questions.id ASC"),
        NUM_ANSWERS_DESC("num_answers_desc",
                "num_answers DESC, questions.posted_at DESC, questions.id ASC"),
        SUM_POINTS_DESC("sum_points_desc",
                "sum_points DESC, questions.posted_at DESC, questions.id ASC");

        private String name;
        private String clause;

        private OrderBy(String name, String clause) {
            this.name = name;
            this.clause = clause;
        }

        public String getName() {
            return name;
        }

        public String getCaluse() {
            return clause;
        }

        public static OrderBy defaultValue() {
            return POSTED_AT_DESC;
        }

        public static OrderBy fromName(String name) {
            for (OrderBy x : OrderBy.values()) {
                if (x.getName().equals(name)) {
                    return x;
                }
            }
            return defaultValue();
        }
    }
}
