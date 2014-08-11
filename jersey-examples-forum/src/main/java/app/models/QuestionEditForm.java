package app.models;

import javax.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

import org.hibernate.validator.constraints.NotEmpty;

public class QuestionEditForm {
    private String id = "";

    @NotEmpty
    private String subject = "";

    @NotEmpty
    private String body = "";

    @NotEmpty
    private String status = "";

    private final Map<String, String> statusMap;

    public QuestionEditForm() {
        this.statusMap = ImmutableMap.of(
                String.valueOf(Question.Status.PUBLISHED.getValue()),
                Question.Status.PUBLISHED.getLabel(),
                String.valueOf(Question.Status.DRAFT.getValue()),
                Question.Status.DRAFT.getLabel());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getStatusMap() {
        return statusMap;
    }

    public static QuestionEditForm emptyForm() {
        return new QuestionEditForm();
    }

    public static QuestionEditForm defaultForm() {
        return new QuestionEditForm();
    }

    public static QuestionEditForm bindFrom(MultivaluedMap<String, String> params) {
        QuestionEditForm form = emptyForm();
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

    public static QuestionEditForm bindFrom(Question question) {
        QuestionEditForm form = emptyForm();
        if (question.getId() != null) {
            form.setId(question.getId().toString());
        }
        form.setSubject(question.getSubject());
        form.setBody(question.getBody());
        form.setStatus(String.valueOf(question.getStatus().getValue()));
        return form;
    }
}
