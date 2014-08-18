package app.models;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.AssertTrue;

import org.hibernate.validator.constraints.NotEmpty;

import javax.ws.rs.core.MultivaluedMap;

public class RecoveryResetForm {
    private String code;

    @NotEmpty(message = "{Account.password.NotEmpty}")
    @Pattern(
            regexp = "^(|\\p{Graph}+)$",
            message = "{Account.password.Pattern}")
    private String password = "";

    private String retypedPassword = "";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRetypedPassword() {
        return retypedPassword;
    }

    public void setRetypedPassword(String retypedPassword) {
        this.retypedPassword = retypedPassword;
    }

    @AssertTrue(message = "{Account.validRetypedPassword.AssertTrue}")
    public boolean isValidRetypedPassword() {
        if (password == null)
            return retypedPassword == null;
        else
            return password.equals(retypedPassword);
    }

    public static RecoveryResetForm emptyForm() {
        return new RecoveryResetForm();
    }

    public static RecoveryResetForm defaultForm() {
        return new RecoveryResetForm();
    }

    public static RecoveryResetForm bindFrom(MultivaluedMap<String, String> params) {
        RecoveryResetForm form = emptyForm();
        if (params.containsKey("code"))
            form.setCode(params.getFirst("code"));
        if (params.containsKey("password"))
            form.setPassword(params.getFirst("password"));
        if (params.containsKey("retypedPassword"))
            form.setRetypedPassword(params.getFirst("retypedPassword"));
        return form;
    }
}
