package app.models;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.core.MultivaluedMap;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.StringUtils;

public class AnswerEditForm {
    private String id = "";

    private String questionId = "";

    @NotEmpty(message = "{Answer.body.NotEmpty}")
    private String body = "";

    @Pattern(regexp = "^(0|2)$", message = "{Answer.status.Pattern}")
    private String status = "";

    private final Map<String, String> statusMap;

    public AnswerEditForm() {
        this.statusMap = ImmutableMap.of(
                String.valueOf(Answer.Status.PUBLISHED.getValue()),
                Answer.Status.PUBLISHED.getLabel(),
                String.valueOf(Question.Status.DRAFT.getValue()),
                Answer.Status.DRAFT.getLabel());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getStatusMap() {
        return statusMap;
    }

    public static AnswerEditForm emptyForm() {
        return new AnswerEditForm();
    }

    public static AnswerEditForm defaultForm() {
        return new AnswerEditForm();
    }

    public static AnswerEditForm bindFrom(MultivaluedMap<String, String> params) {
        AnswerEditForm form = emptyForm();
        try {
            for (String key : params.keySet()) {
                String methodName = "set" + StringUtils.capitalize(key);
                Method m = MethodUtils.getAccessibleMethod(
                        form.getClass(), methodName, String.class);
                if (m != null) {
                    m.invoke(form, params.getFirst(key));
                }
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
        return form;
    }

    public static AnswerEditForm bindFrom(Answer answer) {
        AnswerEditForm form = emptyForm();
        if (answer.getId() != null) {
            form.setId(answer.getId().toString());
        }
        form.setBody(answer.getBody());
        form.setStatus(String.valueOf(answer.getStatus().getValue()));
        return form;
    }
}
