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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.mail.EmailException;

import app.core.*;
import app.models.*;

import static app.core.Util.params;

@Path("/signup")
@Produces(MediaType.TEXT_HTML)
public class SignupController {
    private final Validator validator;
    private final Storage signupStorage;
    private final AccountDao accountDao;
    private final SignupMailerFactory signupMailerFactory;

    public SignupController(
            Storage signupStorage,
            AccountDao accountDao,
            SignupMailerFactory signupMailerFactory) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.signupStorage = signupStorage;
        this.accountDao = accountDao;
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
    public Response confirm(@Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams)
            throws EmailException {
        SignupForm form = SignupForm.bindFrom(formParams);
        Set<ConstraintViolation<SignupForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("signup/entry", params(
                    "form", new FormHelper<SignupForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }
        if (accountDao.findByEmail(form.getEmail()).isPresent()) {
            List<String> messages = ImmutableList.of("The email has already been used.");
            View view = new View("signup/entry", params(
                    "form", new FormHelper<SignupForm>(form, messages)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }

        Map<String, Object> params = params(
                "email", form.getEmail(),
                "password", form.getPassword());
        String code = signupStorage.create(params);
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
    public Response activate(@Context UriInfo uinfo, @QueryParam("code") String code) {
        Optional<?> opt = signupStorage.read(code, Map.class);
        signupStorage.delete(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/signup/errors/session").build()).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) opt.get();
        if (accountDao.findByEmail(params.get("email")).isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/signup/errors/email").build()).build();
        }

        Account account = new Account();
        account.setEmail(params.get("email"));
        account.setStatus(Account.Status.ACTIVE);
        account.refreshPassword(params.get("password"));
        Account savedAccount = accountDao.save(account);
        return Response.ok(new View("signup/activate", params("account", savedAccount))).build();
    }

    @GET
    @Path("errors/{name}")
    public Response errors(@PathParam("name") String name) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new View("signup/errors/" + name))
                .build();
    }
}
