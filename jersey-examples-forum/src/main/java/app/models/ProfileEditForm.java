package app.models;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.AssertTrue;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

import javax.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.StringUtils;

public class ProfileEditForm {
    @NotEmpty(message = "{Account.email.NotEmpty}")
    @Email(message = "{Account.email.Email}")
    private String email = "";

    @Pattern(
            regexp = "^(|\\p{Graph}+)$",
            message = "{Account.currentPassword.Pattern}")
    private String currentPassword = "";

    @Pattern(
            regexp = "^(|\\p{Graph}+)$",
            message = "{Account.password.Pattern}")
    private String password = "";

    private String retypedPassword = "";

    private String nickname = "";

    private String iconToken = "";

    @AssertTrue(message = "{Account.validCurrentPassword.AssertTrue}")
    private boolean validCurrentPassword = true;

    @AssertTrue(message = "{Account.uniqueEmail.AssertTrue}")
    private boolean uniqueEmail = true;

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        this.email = v;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIconToken() {
        return iconToken;
    }

    public void setIconToken(String iconToken) {
        this.iconToken = iconToken;
    }

    public boolean isValidCurrentPassword() {
        return validCurrentPassword;
    }

    public void setValidCurrentPassword(boolean validCurrentPassword) {
        this.validCurrentPassword = validCurrentPassword;
    }

    public boolean isUniqueEmail() {
        return uniqueEmail;
    }

    public void setUniqueEmail(boolean uniqueEmail) {
        this.uniqueEmail = uniqueEmail;
    }

    @AssertTrue(message = "{Account.currentPassword.NotEmpty}")
    public boolean hasCurrentPassword() {
        return (password == null || password.isEmpty())
                || (currentPassword != null && !currentPassword.isEmpty());
    }

    @AssertTrue(message = "{Account.validRetypedPassword.AssertTrue}")
    public boolean isValidRetypedPassword() {
        if (password == null)
            return retypedPassword == null;
        else
            return password.equals(retypedPassword);
    }

    public static ProfileEditForm emptyForm() {
        return new ProfileEditForm();
    }

    public static ProfileEditForm defaultForm() {
        return new ProfileEditForm();
    }

    public static ProfileEditForm bindFrom(MultivaluedMap<String, String> params) {
        ProfileEditForm form = emptyForm();
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

    public static ProfileEditForm bindFrom(Account account) {
        ProfileEditForm form = emptyForm();
        form.setEmail(account.getEmail());
        form.setNickname(account.getNickname());
        return form;
    }
}
