package app.models;

import javax.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.StringUtils;

import org.hibernate.validator.constraints.NotEmpty;

public class ArticleEditForm {
    private String id = "";

    private String parentId = "";

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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    public static ArticleEditForm emptyForm() {
        return new ArticleEditForm();
    }

    public static ArticleEditForm defaultForm() {
        return new ArticleEditForm();
    }

    public static ArticleEditForm bindFrom(MultivaluedMap<String, String> params) {
        ArticleEditForm form = emptyForm();
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

    public static ArticleEditForm bindFrom(Article article) {
        ArticleEditForm form = emptyForm();
        if (article.getId() != null) {
            form.setId(article.getId().toString());
        }
        form.setSubject(article.getSubject());
        form.setBody(article.getBody());
        return form;
    }
}
