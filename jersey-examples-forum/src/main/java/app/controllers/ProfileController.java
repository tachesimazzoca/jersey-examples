package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.servlet.http.HttpServletRequest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Set;

import app.core.*;
import app.models.*;

import static app.core.Util.params;

@Path("/profile")
@Produces(MediaType.TEXT_HTML)
public class ProfileController {
    private final Validator validator;
    private final CookieBakerFactory loginCookieFactory;
    private final AccountDao accountDao;

    public ProfileController(
            CookieBakerFactory loginCookieFactory,
            AccountDao accountDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.loginCookieFactory = loginCookieFactory;
        this.accountDao = accountDao;
    }

    @GET
    @Path("edit")
    public Response edit(
            @Context UriInfo uinfo,
            @Context HttpServletRequest req) {
        final Response redirect = redirectToLogin(uinfo);

        CookieBaker login = loginCookieFactory.create(req);
        if (!login.get("id").isPresent()) {
            return redirect;
        }
        Optional<Account> accountOpt = accountDao.find(Long.parseLong(login.get("id").get()));
        if (!accountOpt.isPresent()) {
            return redirect;
        }

        Account account = accountOpt.get();
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
            @Context UriInfo uinfo,
            @Context HttpServletRequest req,
            MultivaluedMap<String, String> formParams) {

        final Response redirect = redirectToLogin(uinfo);

        CookieBaker login = loginCookieFactory.create(req);
        if (!login.get("id").isPresent()) {
            return redirect;
        }
        Optional<Account> accountOpt = accountDao.find(Long.parseLong(login.get("id").get()));
        if (!accountOpt.isPresent()) {
            return redirect;
        }
        Account account = accountOpt.get();

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

        account.setEmail(form.getEmail());
        if (!form.getPassword().isEmpty()) {
            account.refreshPassword(form.getPassword());
        }
        accountDao.save(account);

        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/").build()).build();
    }

    private Response redirectToLogin(UriInfo uinfo) {
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/auth/login")
                .queryParam("url", "/profile/edit")
                .build()).build();
    }
}
