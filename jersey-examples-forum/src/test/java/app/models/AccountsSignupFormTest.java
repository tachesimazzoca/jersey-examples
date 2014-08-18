package app.models;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class AccountsSignupFormTest {
    private final Validator validator = Validation
            .buildDefaultValidatorFactory().getValidator();

    private <T> Set<String> convertToMessages(
            Set<ConstraintViolation<T>> results) {
        ImmutableSet.Builder<String> builder = ImmutableSet.<String> builder();
        for (ConstraintViolation<T> result : results) {
            builder.add(result.getMessage());
        }
        return builder.build();
    }

    @Test
    public void testConstraints() {
        AccountsSignupForm form = new AccountsSignupForm();
        Set<ConstraintViolation<AccountsSignupForm>> results;

        form.setEmail("");
        form.setPassword("1234");
        form.setRetypedPassword("1234");
        results = validator.validate(form);
        assertEquals(1, results.size());
        assertEquals(ImmutableSet.of("Email may not be empty"),
                convertToMessages(results));

        form.setEmail("-invalid$email");
        results = validator.validate(form);
        assertEquals(1, results.size());
        assertEquals(ImmutableSet.of("Email is not a well-formed email address"),
                convertToMessages(results));

        form.setEmail("foo@example.net");
        form.setPassword("");
        results = validator.validate(form);
        assertEquals(2, results.size());
        assertEquals(ImmutableSet.of(
                "Password may not be empty",
                "Password must be equal to Re-type Password"),
                convertToMessages(results));

        form.setPassword(" ");
        form.setRetypedPassword(" ");
        results = validator.validate(form);
        assertEquals(1, results.size());
        assertEquals(ImmutableSet.of(
                "Password must match visible characters"),
                convertToMessages(results));

        form.setPassword("0000");
        form.setRetypedPassword("0000");
        form.setUniqueEmail(false);
        results = validator.validate(form);
        assertEquals(1, results.size());
        assertEquals(ImmutableSet.of(
                "Email has already been used"),
                convertToMessages(results));

        form.setUniqueEmail(true);
        results = validator.validate(form);
        assertEquals(0, results.size());
    }
}
