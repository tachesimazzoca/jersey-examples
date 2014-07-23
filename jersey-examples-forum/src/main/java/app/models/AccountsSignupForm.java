package app.models;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.AssertTrue;

import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

public class AccountsSignupForm {
    @NotEmpty
    @Email
    private String email = "";

    @NotEmpty
    @Pattern(regexp = "^(|\\p{Graph}+)$")
    private String password = "";

    @NotEmpty
    private String retypedPassword = "";

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

    @AssertTrue(message = "password should match retyped password")
    public boolean isValidRetypedPassword() {
        if (password == null)
            return retypedPassword == null;
        else
            return password.equals(retypedPassword);
    }
}
