package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.Set;

import com.google.common.collect.ImmutableMap;

import app.core.View;
import app.core.FormHelper;
import app.models.SignupForm;

@Path("/signup")
public class SignupController {
    private final Validator validator;

    public SignupController() {
        this.validator =
                Validation.buildDefaultValidatorFactory().getValidator();
    }

    @GET
    @Path("entry")
    public Response entry() {
        View view = new View(
                "signup/entry",
                ImmutableMap.<String, Object> of(
                        "form",
                        new FormHelper<SignupForm>(SignupForm.defaultForm())));
        return Response.ok(view).build();
    }

    @POST
    @Path("entry")
    @Consumes("application/x-www-form-urlencoded")
    public Response confirm(@Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {
        SignupForm form = SignupForm.bindFrom(formParams);
        Set<ConstraintViolation<SignupForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View(
                    "signup/entry",
                    ImmutableMap.<String, Object> of(
                            "form",
                            new FormHelper<SignupForm>(form, errors)));

            return Response.ok(view).build();
        }

        return Response.seeOther(
                uinfo.getBaseUriBuilder().path("/signup/verify").build())
                .build();
    }

    @GET
    @Path("verify")
    public Response verify() {
        return Response.ok(new View("todo")).build();
    }
}
