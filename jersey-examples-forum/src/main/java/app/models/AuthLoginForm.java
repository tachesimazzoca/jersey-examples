package app.models;

import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

public class AuthLoginForm {
    @NotEmpty
    @Email
    private String email = "";

    @NotEmpty
    private String password = "";

    private String url = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        this.email = v;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String v) {
        this.password = v;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static AuthLoginForm emptyForm() {
        return new AuthLoginForm();
    }

    public static AuthLoginForm defaultForm() {
        return new AuthLoginForm();
    }

    public static AuthLoginForm bindFrom(MultivaluedMap<String, String> params) {
        AuthLoginForm form = AuthLoginForm.emptyForm();
        if (params.containsKey("email"))
            form.setEmail(params.getFirst("email"));
        if (params.containsKey("password"))
            form.setPassword(params.getFirst("password"));
        if (params.containsKey("url"))
            form.setUrl(params.getFirst("url"));
        return form;
    }
}
