package app.models;

import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

public class AccountsSigninForm {
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

    public static AccountsSigninForm emptyForm() {
        return new AccountsSigninForm();
    }

    public static AccountsSigninForm defaultForm() {
        return new AccountsSigninForm();
    }

    public static AccountsSigninForm bindFrom(MultivaluedMap<String, String> params) {
        AccountsSigninForm form = emptyForm();
        if (params.containsKey("email"))
            form.setEmail(params.getFirst("email"));
        if (params.containsKey("password"))
            form.setPassword(params.getFirst("password"));
        if (params.containsKey("url"))
            form.setUrl(params.getFirst("url"));
        return form;
    }
}
