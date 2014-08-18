package app.models;

import javax.validation.constraints.AssertTrue;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

import javax.ws.rs.core.MultivaluedMap;

public class RecoveryEntryForm {
    @NotEmpty(message = "{Account.email.NotEmpty}")
    @Email(message = "{Account.email.Email}")
    private String email = "";

    @AssertTrue(message = "{Account.activeEmail.AssertTrue}")
    private boolean activeEmail = true;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActiveEmail() {
        return activeEmail;
    }

    public void setActiveEmail(boolean activeEmail) {
        this.activeEmail = activeEmail;
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
