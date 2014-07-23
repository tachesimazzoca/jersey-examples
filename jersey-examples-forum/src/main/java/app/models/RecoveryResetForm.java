package app.models;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.AssertTrue;

import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.validator.constraints.NotEmpty;

public class RecoveryResetForm {
    private String code;

    @NotEmpty
    @Pattern(regexp = "^(|\\p{Graph}+)$")
    private String password = "";

    @NotEmpty
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

    public void setPassword(String v) {
        this.password = v;
    }

    public String getRetypedPassword() {
        return retypedPassword;
    }

    public void setRetypedPassword(String v) {
        this.retypedPassword = v;
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

    @AssertTrue(message = "password should match retyped password")
    public boolean isValidRetypedPassword() {
        if (password == null)
            return retypedPassword == null;
        else
            return password.equals(retypedPassword);
    }
}
