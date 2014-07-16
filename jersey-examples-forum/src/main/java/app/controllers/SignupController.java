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
import app.models.User;
import app.models.UserDao;
import app.models.SignupForm;

import static app.core.Util.params;

@Path("/signup")
public class SignupController {
    private final Validator validator;
    private final Storage signupStorage;
    private final UserDao userDao;

    public SignupController(Storage signupStorage, UserDao userDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();

        this.signupStorage = signupStorage;
        this.userDao = userDao;
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

        if (userDao.findByEmail(form.getEmail()).isPresent()) {
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
        if (!ser.isPresent()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, String> params = Jackson.newObjectMapper().readValue(ser.get(), Map.class);

        if (userDao.findByEmail(params.get("email")).isPresent()) {
            return Response.status(Response.Status.FORBIDDEN).entity("The email is already used")
                    .build();
        }
        User user = new User();
        user.setEmail(params.get("email"));
        user.setStatus(User.Status.ACTIVATED);
        user.updatePassword(params.get("password"));
        User savedUser = userDao.save(user);

        return Response.ok(savedUser.getPasswordHash()).type(MediaType.TEXT_PLAIN).build();
    }
}
