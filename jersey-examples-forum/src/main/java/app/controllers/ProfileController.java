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

import app.core.*;
import app.mail.TextMailerFactory;
import app.models.*;

import static app.core.Util.params;

@Path("/profile")
@Produces(MediaType.TEXT_HTML)
public class ProfileController {
    private final Validator validator;
    private final AccountDao accountDao;
    private final Storage profileStorage;
    private final TextMailerFactory profileMailerFactory;

    public ProfileController(
            AccountDao accountDao,
            Storage profileStorage,
            TextMailerFactory profileMailerFactory) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.accountDao = accountDao;
        this.profileStorage = profileStorage;
        this.profileMailerFactory = profileMailerFactory;
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context User user,
            @Context UriInfo uinfo) {
        final Response redirect = redirectToLogin(uinfo);

        if (!user.getAccount().isPresent()) {
            return redirect;
        }
        Account account = user.getAccount().get();

        ProfileEditForm form = ProfileEditForm.defaultForm();
        form.setEmail(account.getEmail());

        View view = new View("profile/edit", params(
                "form", new FormHelper<ProfileEditForm>(form)));
        return Response.ok(view).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response confirm(
            @Context User user,
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {

        final Response redirect = redirectToLogin(uinfo);

        if (!user.getAccount().isPresent()) {
            return redirect;
        }
        Account account = user.getAccount().get();

        ProfileEditForm form = ProfileEditForm.bindFrom(formParams);
        Set<ConstraintViolation<ProfileEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("profile/edit", params(
                    "form", new FormHelper<ProfileEditForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        if (!form.getCurrentPassword().isEmpty()) {
            if (!account.isEqualPassword(form.getCurrentPassword())) {
                List<String> messages = ImmutableList.<String> of("Invalid password");
                View view = new View("profile/edit", params(
                        "form", new FormHelper<ProfileEditForm>(form, messages)));
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(view).build();
            }
        }

        if (!form.getEmail().equals(account.getEmail())) {
            if (accountDao.findByEmail(form.getEmail()).isPresent()) {
                List<String> messages = ImmutableList.of("The email has already been used.");
                View view = new View("profile/edit", params(
                        "form", new FormHelper<ProfileEditForm>(form, messages)));
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(view).build();
            }
        }

        if (!form.getPassword().isEmpty()) {
            account.refreshPassword(form.getPassword());
        }
        accountDao.save(account);

        if (form.getEmail().equals(account.getEmail())) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/").build()).build();
        }

        Map<String, Object> params = params(
                "id", account.getId(),
                "email", form.getEmail()
                );
        String code = profileStorage.create(params);
        String url = uinfo.getBaseUriBuilder()
                .path("/profile/activate")
                .queryParam("code", code)
                .build()
                .toString();
        profileMailerFactory.create(form.getEmail(), url).send();

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/profile/verify").build()).build();
    }

    @GET
    @Path("verify")
    public Response verify() {
        return Response.ok(new View("profile/verify")).build();
    }

    @GET
    @Path("activate")
    public Response activate(
            @Context User user,
            @Context UriInfo uinfo,
            @QueryParam("code") String code) {

        final Optional<?> opt = profileStorage.read(code, Map.class);
        profileStorage.delete(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/errors/session").build()).build();
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> params = (Map<String, Object>) opt.get();
        final Long id = (Long) params.get("id");
        final String email = (String) params.get("email");

        if (user.getAccount().isPresent()) {
            Account account = user.getAccount().get();
            if (id != account.getId()) {
                // The current user is not a verified user.
                return Response.seeOther(uinfo.getBaseUriBuilder()
                        .path("/profile/errors/session").build()).build();
            }
        }

        final Optional<Account> accountOpt = accountDao.find(id);
        if (!accountOpt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/errors/session").build()).build();
        }
        if (accountDao.findByEmail(email).isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/errors/email").build()).build();
        }

        final Account account = accountOpt.get();
        account.setEmail(email);
        final Account savedAccount = accountDao.save(account);
        return Response.ok(new View("profile/activate",
                params("account", savedAccount))).build();
    }

    @GET
    @Path("errors/{name}")
    public Response errors(@PathParam("name") String name) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new View("profile/errors/" + name))
                .build();
    }

    private Response redirectToLogin(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/auth/login")
                .queryParam("url", "/profile/edit")
                .build()).build();
    }
}
