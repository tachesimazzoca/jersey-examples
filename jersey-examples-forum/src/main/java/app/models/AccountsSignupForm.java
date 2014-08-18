package app.models;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.AssertTrue;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

import javax.ws.rs.core.MultivaluedMap;

public class AccountsSignupForm {
    @NotEmpty(message = "{Account.email.NotEmpty}")
    @Email(message = "{Account.email.Email}")
    private String email = "";

    @NotEmpty(message = "{Account.password.NotEmpty}")
    @Pattern(
            regexp = "^(|\\p{Graph}+)$",
            message = "{Account.password.Pattern}")
    private String password = "";

    private String retypedPassword = "";

    @AssertTrue(message = "{Account.uniqueEmail.AssertTrue}")
    private boolean uniqueEmail = true;

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

    public String getRetypedPassword() {
        return retypedPassword;
    }

    public void setRetypedPassword(String v) {
        this.retypedPassword = v;
    }

    public boolean isUniqueEmail() {
        return uniqueEmail;
    }

    public void setUniqueEmail(boolean uniqueEmail) {
        this.uniqueEmail = uniqueEmail;
    }

    @AssertTrue(message = "{Account.validRetypedPassword.AssertTrue}")
    public boolean isValidRetypedPassword() {
        if (password == null)
            return retypedPassword == null;
        else
            return password.equals(retypedPassword);
    }

    public static AccountsSignupForm emptyForm() {
        return new AccountsSignupForm();
    }

    public static AccountsSignupForm defaultForm() {
        return new AccountsSignupForm();
    }

    public static AccountsSignupForm bindFrom(MultivaluedMap<String, String> params) {
        AccountsSignupForm form = emptyForm();
        if (params.containsKey("email"))
            form.setEmail(params.getFirst("email"));
        if (params.containsKey("password"))
            form.setPassword(params.getFirst("password"));
        if (params.containsKey("retypedPassword"))
            form.setRetypedPassword(params.getFirst("retypedPassword"));
        return form;
    }
}
