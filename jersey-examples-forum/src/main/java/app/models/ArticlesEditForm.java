package app.models;

import javax.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.StringUtils;

import org.hibernate.validator.constraints.NotEmpty;

public class ArticlesEditForm {
    private String id = "";

    private String authorId = "";

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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
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

    public static ArticlesEditForm emptyForm() {
        return new ArticlesEditForm();
    }

    public static ArticlesEditForm defaultForm() {
        return new ArticlesEditForm();
    }

    public static ArticlesEditForm bindFrom(MultivaluedMap<String, String> params) {
        ArticlesEditForm form = emptyForm();
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

    public static ArticlesEditForm bindFrom(Article article) {
        ArticlesEditForm form = emptyForm();
        if (article.getId() != null) {
            form.setId(article.getId().toString());
        }
        if (article.getParentId() != 0) {
            form.setParentId(article.getParentId().toString());
        }
        if (article.getAuthorId() != 0) {
            form.setAuthorId(article.getAuthorId().toString());
        }
        form.setSubject(article.getSubject());
        form.setBody(article.getBody());
        return form;
    }
}
