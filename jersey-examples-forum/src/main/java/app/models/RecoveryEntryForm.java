package app.models;

import javax.ws.rs.core.MultivaluedMap;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

public class RecoveryEntryForm {
    @NotEmpty
    @Email
    private String email = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        this.email = v;
    }

    public static RecoveryEntryForm emptyForm() {
        return new RecoveryEntryForm();
    }

    public static RecoveryEntryForm defaultForm() {
        return new RecoveryEntryForm();
    }

    public static RecoveryEntryForm bindFrom(MultivaluedMap<String, String> params) {
        RecoveryEntryForm form = emptyForm();
        if (params.containsKey("email"))
            form.setEmail(params.getFirst("email"));
        return form;
    }
}
