package app.models;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class SignupFormTest {
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
        SignupForm form = new SignupForm();
        Set<ConstraintViolation<SignupForm>> results;

        form.setEmail("");
        form.setPassword("1234");
        form.setRetypedPassword("1234");
        results = validator.validate(form);
        assertEquals(1, results.size());
        assertEquals(ImmutableSet.of("may not be empty"),
                convertToMessages(results));

        form.setEmail("-invalid$email");
        results = validator.validate(form);
        assertEquals(1, results.size());
        assertEquals(ImmutableSet.of("not a well-formed email address"),
                convertToMessages(results));

        form.setEmail("foo@example.net");
        results = validator.validate(form);
        assertTrue(results.isEmpty());
    }
}
