package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import com.google.common.base.Optional;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import app.core.FormHelper;
import app.core.Jackson;
import app.core.Storage;
import app.core.View;
import app.models.SignupForm;

import static app.core.Util.params;

@Path("/signup")
public class SignupController {
    private final Storage signupStorage;
    private final Validator validator;

    public SignupController(Storage signupStorage) {
        this.signupStorage = signupStorage;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @GET
    @Path("entry")
    public Response entry() {
        View view = new View("signup/entry", params(
                "form", new FormHelper<SignupForm>(SignupForm.defaultForm())));
        return Response.ok(view).build();
    }

    @POST
    @Path("entry")
    @Consumes("application/x-www-form-urlencoded")
    public Response confirm(@Context UriInfo uinfo, MultivaluedMap<String, String> formParams)
            throws JsonProcessingException {
        SignupForm form = SignupForm.bindFrom(formParams);
        Set<ConstraintViolation<SignupForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("signup/entry", params(
                    "form", new FormHelper<SignupForm>(form, errors)));

            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }

        Map<String, Object> params = params(
                "email", form.getEmail(),
                "password", form.getPassword());
        String serialized = Jackson.newObjectMapper().writeValueAsString(params);
        String code = signupStorage.create(serialized);

        return Response.seeOther(
                uinfo.getBaseUriBuilder().path("/signup/verify")
                        .queryParam("code", code).build()).build();
    }

    @GET
    @Path("verify")
    public Response verify(@QueryParam("code") String code) throws
            JsonParseException,
            JsonMappingException,
            IOException {
        Optional<String> ser = signupStorage.read(code);
        @SuppressWarnings("unchecked")
        Map<String, String> params = Jackson.newObjectMapper().readValue(ser.get(), Map.class);

        final String LF = String.format("%n");
        StringBuilder sb = new StringBuilder();
        sb.append("Email: ");
        sb.append(params.get("email"));
        sb.append(LF);
        sb.append("Password: ");
        sb.append(params.get("password"));
        sb.append(LF);

        return Response.ok(sb.toString()).type(MediaType.TEXT_PLAIN).build();
    }
}
