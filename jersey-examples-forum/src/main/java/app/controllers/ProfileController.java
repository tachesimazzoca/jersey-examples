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

import java.util.Map;
import java.util.Set;

import app.core.FormHelper;
import app.core.Storage;
import app.core.View;
import app.mail.TextMailerFactory;
import app.models.Account;
import app.models.AccountDao;
import app.models.ProfileEditForm;
import app.models.UserContext;

import static app.core.Util.params;

@Path("/profile")
@Produces(MediaType.TEXT_HTML)
public class ProfileController {
    private final Validator validator;
    private final AccountDao accountDao;
    private final Storage<Map<String, Object>> profileStorage;
    private final TextMailerFactory profileMailerFactory;

    public ProfileController(
            AccountDao accountDao,
            Storage<Map<String, Object>> profileStorage,
            TextMailerFactory profileMailerFactory) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.accountDao = accountDao;
        this.profileStorage = profileStorage;
        this.profileMailerFactory = profileMailerFactory;
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @QueryParam("flash") @DefaultValue("") String flash) {
        Optional<Account> accountOpt = userContext.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo);
        Account account = accountOpt.get();

        ProfileEditForm form = ProfileEditForm.bindFrom(account);
        View view = new View("profile/edit", params(
                "account", account,
                "form", new FormHelper<ProfileEditForm>(form),
                "flash", userContext.getFlash().orNull()));
        return Response.ok(view).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {
        Optional<Account> accountOpt = userContext.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo);
        Account account = accountOpt.get();

        ProfileEditForm form = ProfileEditForm.bindFrom(formParams);
        if (validator.validateProperty(form, "email").isEmpty()) {
            if (!form.getEmail().equals(account.getEmail())) {
                if (accountDao.findByEmail(form.getEmail()).isPresent()) {
                    form.setUniqueEmail(false);
                }
            }
        }
        if (!form.getCurrentPassword().isEmpty()
                && validator.validateProperty(form, "currentPassword").isEmpty()) {
            if (!account.isEqualPassword(form.getCurrentPassword())) {
                form.setValidCurrentPassword(false);
            }
        }
        Set<ConstraintViolation<ProfileEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("profile/edit", params(
                    "account", account,
                    "form", new FormHelper<ProfileEditForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        account.setNickname(form.getNickname());
        if (!form.getPassword().isEmpty()) {
            account.refreshPassword(form.getPassword());
        }
        accountDao.save(account);

        if (form.getEmail().equals(account.getEmail())) {
            userContext.setFlash("saved");
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/edit").build())
                    .cookie(userContext.toCookie()).build();
        }

        Map<String, Object> params = params(
                "id", account.getId(),
                "email", form.getEmail());
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
    public Response verify(@Context UserContext userContext) {
        return Response.ok(new View("profile/verify", params(
                "account", userContext.getAccount().orNull()))).build();
    }

    @GET
    @Path("activate")
    public Response activate(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @QueryParam("code") String code) {
        Optional<Map<String, Object>> opt = profileStorage.read(code);
        profileStorage.delete(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/errors/session").build()).build();
        }

        Map<String, Object> params = opt.get();
        Long id = (Long) params.get("id");
        String email = (String) params.get("email");

        Optional<Account> accountOpt = userContext.getAccount();
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (!id.equals(account.getId())) {
                // The current user is not a verified user.
                return Response.seeOther(uinfo.getBaseUriBuilder()
                        .path("/profile/errors/session").build()).build();
            }
        }

        accountOpt = accountDao.find(id);
        if (!accountOpt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/errors/session").build()).build();
        }
        if (accountDao.findByEmail(email).isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/errors/email").build()).build();
        }

        Account account = accountOpt.get();
        account.setEmail(email);
        Account savedAccount = accountDao.save(account);
        return Response.ok(new View("profile/activate", params(
                "account", savedAccount))).build();
    }

    @GET
    @Path("errors/{name}")
    public Response errors(
            @Context UserContext userContext,
            @PathParam("name") String name) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new View("profile/errors/" + name, params(
                        "account", userContext.getAccount().orNull()))).build();
    }

    private Response redirectToLogin(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("returnTo", "/profile/edit")
                .build()).build();
    }
}
