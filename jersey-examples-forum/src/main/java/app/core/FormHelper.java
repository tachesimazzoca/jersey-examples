package app.core;

import java.util.Set;
import java.lang.reflect.InvocationTargetException;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.reflect.MethodUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class FormHelper<T> {
    private final T form;
    private final Set<ConstraintViolation<T>> errors;
    private final List<String> messages;

    public FormHelper(T form) {
        this.form = form;
        this.errors = ImmutableSet.of();
        this.messages = ImmutableList.of();
    }

    public FormHelper(T form, Set<ConstraintViolation<T>> errors) {
        this.form = form;
        this.errors = errors;
        this.messages = ImmutableList.of();
    }

    public FormHelper(T form, List<String> messages) {
        this.form = form;
        this.errors = ImmutableSet.of();
        this.messages = messages;
    }

    public FormHelper(T form, Set<ConstraintViolation<T>> errors, List<String> messages) {
        this.form = form;
        this.errors = errors;
        this.messages = messages;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasMessages() {
        return !messages.isEmpty();
    }

    public T getForm() {
        return form;
    }

    public Set<ConstraintViolation<T>> getErrors() {
        return errors;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String toHTMLInput(String type, String name) throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        return toHTMLInput(type, name, "");
    }

    public String toHTMLInput(String type, String name, String attr) throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        String v = (String) MethodUtils.invokeMethod(
                form, "get" + StringUtils.capitalize(name), null);
        String attrStr = "";
        if (!attr.isEmpty()) {
            attrStr = " " + attr;
        }
        return String.format(
                "<input type=\"%s\" name=\"%s\" value=\"%s\"%s>", type, name,
                StringEscapeUtils.escapeHtml(v), attrStr);
    }
}
