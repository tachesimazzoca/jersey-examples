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
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.mail.EmailException;

import app.core.FormHelper;
import app.core.Jackson;
import app.core.Storage;
import app.core.View;
import app.models.User;
import app.models.UserDao;
import app.models.SignupForm;
import app.models.SignupMailerFactory;

import static app.core.Util.params;

@Path("/signup")
@Produces(MediaType.TEXT_HTML)
public class SignupController {
    private final Validator validator;
    private final Storage signupStorage;
    private final UserDao userDao;
    private final SignupMailerFactory signupMailerFactory;

    public SignupController(
            Storage signupStorage,
            UserDao userDao,
            SignupMailerFactory signupMailerFactory) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();

        this.signupStorage = signupStorage;
        this.userDao = userDao;
        this.signupMailerFactory = signupMailerFactory;
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
            throws JsonProcessingException, EmailException {
        SignupForm form = SignupForm.bindFrom(formParams);
        Set<ConstraintViolation<SignupForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("signup/entry", params(
                    "form", new FormHelper<SignupForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }
        if (userDao.findByEmail(form.getEmail()).isPresent()) {
            List<String> messages = ImmutableList.of("The email has already been used.");
            View view = new View("signup/entry", params(
                    "form", new FormHelper<SignupForm>(form, messages)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }

        Map<String, Object> params = params(
                "email", form.getEmail(),
                "password", form.getPassword());
        String serialized = Jackson.newObjectMapper().writeValueAsString(params);
        String code = signupStorage.create(serialized);
        String url = uinfo.getBaseUriBuilder()
                .path("/signup/activate")
                .queryParam("code", code)
                .build()
                .toString();
        signupMailerFactory.create(form.getEmail(), url).send();

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/signup/verify").build()).build();
    }

    @GET
    @Path("verify")
    public Response verify() {
        return Response.ok(new View("signup/verify")).build();
    }

    @GET
    @Path("activate")
    public Response activate(@Context UriInfo uinfo, @QueryParam("code") String code) throws
            JsonParseException,
            JsonMappingException,
            IOException {
        Optional<String> ser = signupStorage.read(code);
        signupStorage.delete(code);
        if (!ser.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/signup/errors/session").build()).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, String> params = Jackson.newObjectMapper().readValue(ser.get(), Map.class);
        if (userDao.findByEmail(params.get("email")).isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/signup/errors/email").build()).build();
        }

        User user = new User();
        user.setEmail(params.get("email"));
        user.setStatus(User.Status.ACTIVE);
        user.refreshPassword(params.get("password"));
        User savedUser = userDao.save(user);
        return Response.ok(new View("signup/activate", params("user", savedUser))).build();
    }

    @GET
    @Path("errors/{name}")
    public Response errors(@PathParam("name") String name) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new View("signup/errors/" + name))
                .build();
    }
}
