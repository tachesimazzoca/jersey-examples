package app.models;

import javax.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.StringUtils;

import org.hibernate.validator.constraints.NotEmpty;

public class QuestionsEditForm {
    private String id = "";

    @NotEmpty
    private String subject = "";

    @NotEmpty
    private String body = "";

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

    public static QuestionsEditForm emptyForm() {
        return new QuestionsEditForm();
    }

    public static QuestionsEditForm defaultForm() {
        return new QuestionsEditForm();
    }

    public static QuestionsEditForm bindFrom(MultivaluedMap<String, String> params) {
        QuestionsEditForm form = emptyForm();
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

    public static QuestionsEditForm bindFrom(Question question) {
        QuestionsEditForm form = emptyForm();
        if (question.getId() != null) {
            form.setId(question.getId().toString());
        }
        form.setSubject(question.getSubject());
        form.setBody(question.getBody());
        return form;
    }
}
