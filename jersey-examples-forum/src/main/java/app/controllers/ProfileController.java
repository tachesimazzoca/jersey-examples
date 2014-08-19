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
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("flash") @DefaultValue("") String flash) {
        Optional<Account> accountOpt = getAccount(session);
        if (!accountOpt.isPresent())
            return redirectToLogin(uinfo);
        Account account = accountOpt.get();

        ProfileEditForm form = ProfileEditForm.bindFrom(account);
        View view = new View("profile/edit", params(
                "form", new FormHelper<ProfileEditForm>(form),
                "flash", flash));
        return Response.ok(view).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @Context Session session,
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {
        Optional<Account> accountOpt = getAccount(session);
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
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/edit").queryParam("flash", "saved").build()).build();
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
    public Response verify() {
        return Response.ok(new View("profile/verify")).build();
    }

    @GET
    @Path("activate")
    public Response activate(
            @Context Session session,
            @Context UriInfo uinfo,
            @QueryParam("code") String code) {
        Optional<?> opt = profileStorage.read(code, Map.class);
        profileStorage.delete(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/profile/errors/session").build()).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) opt.get();
        Long id = (Long) params.get("id");
        String email = (String) params.get("email");

        Optional<Account> accountOpt = getAccount(session);
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

    private Optional<Account> getAccount(Session session) {
        Optional<String> accountId = session.get("accountId");
        if (!accountId.isPresent())
            return Optional.absent();
        return accountDao.find(Long.parseLong(accountId.get()));
    }

    private Response redirectToLogin(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("returnTo", "/profile/edit")
                .build()).build();
    }
}
