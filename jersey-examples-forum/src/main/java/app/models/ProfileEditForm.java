package app.models;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.AssertTrue;

import javax.ws.rs.core.MultivaluedMap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.commons.lang.StringUtils;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

public class ProfileEditForm {
    @NotEmpty
    @Email
    private String email = "";

    @Pattern(regexp = "^(|\\p{Graph}+)$")
    private String currentPassword = "";

    @Pattern(regexp = "^(|\\p{Graph}+)$")
    private String password = "";

    private String retypedPassword = "";

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

    @AssertTrue(message = "current password is required")
    public boolean hasCurrentPassword() {
        return (password == null || password.isEmpty())
                || (currentPassword != null && !currentPassword.isEmpty());
    }

    @AssertTrue(message = "password should match retyped password")
    public boolean isValidRetypedPassword() {
        if (password == null)
            return retypedPassword == null;
        else
            return password.equals(retypedPassword);
    }
}
