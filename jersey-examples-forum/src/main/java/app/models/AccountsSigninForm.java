package app.models;

import javax.ws.rs.core.MultivaluedMap;

import javax.validation.constraints.AssertTrue;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

public class AccountsSigninForm {
    @NotEmpty(message = "{Account.email.NotEmpty}")
    @Email(message = "{Account.email.Email}")
    private String email = "";

    @NotEmpty(message = "{Account.password.NotEmpty}")
    private String password = "";

    private String returnTo = "";

    @AssertTrue(message = "{Account.authorized.AssertTrue}")
    private boolean authorized = true;

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

    public String getReturnTo() {
        return returnTo;
    }

    public void setReturnTo(String returnTo) {
        this.returnTo = returnTo;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
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
        if (params.containsKey("returnTo"))
            form.setReturnTo(params.getFirst("returnTo"));
        return form;
    }
}
