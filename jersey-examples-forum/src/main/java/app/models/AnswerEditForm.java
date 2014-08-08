package app.models;

import javax.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.StringUtils;

import org.hibernate.validator.constraints.NotEmpty;

public class AnswerEditForm {
    private String id = "";

    private String questionId = "";

    @NotEmpty
    private String body = "";

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
        return form;
    }
}
