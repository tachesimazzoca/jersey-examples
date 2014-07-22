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
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import app.core.*;
import app.models.*;
import static app.core.Util.params;

@Path("/auth")
@Produces(MediaType.TEXT_HTML)
public class AuthController {
    private final Validator validator;
    private final CookieBakerFactory loginCookieFactory;
    private final AccountDao accountDao;

    public AuthController(
            CookieBakerFactory loginCookieFactory,
            AccountDao accountDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.loginCookieFactory = loginCookieFactory;
        this.accountDao = accountDao;
    }

    @GET
    @Path("login")
    public Response login(
            @Context HttpServletRequest req,
            @QueryParam("url") @DefaultValue("") String url) {
        AuthLoginForm form = AuthLoginForm.defaultForm();
        form.setUrl(url);
        CookieBaker login = loginCookieFactory.create();
        return Response.ok(new View("auth/login", params(
                "form", new FormHelper<AuthLoginForm>(form))))
                .cookie(login.toDiscardingCookie()).build();
    }

    @POST
    @Path("login")
    @Consumes("application/x-www-form-urlencoded")
    public Response authenticate(
            @Context UriInfo uinfo,
            @Context HttpServletRequest req,
            MultivaluedMap<String, String> formParams) {

        AuthLoginForm form = AuthLoginForm.bindFrom(formParams);
        Set<ConstraintViolation<AuthLoginForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("auth/login", params(
                    "form", new FormHelper<AuthLoginForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        Optional<Account> accountOpt = accountDao.findByEmail(form.getEmail());
        if (!accountOpt.isPresent() || !accountOpt.get().isEqualPassword(form.getPassword())) {
            List<String> messages = ImmutableList.of("Invalid password or e-mail");
            View view = new View("auth/login", params(
                    "form", new FormHelper<AuthLoginForm>(form, messages)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }
        Account account = accountOpt.get();

        CookieBaker login = loginCookieFactory.create();
        login.put("id", account.getId().toString());

        String url = form.getUrl();
        if (!url.startsWith("/") || url.isEmpty())
            url = "/";
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path(url).build()).cookie(login.toCookie()).build();
    }

    @GET
    @Path("logout")
    public Response logout(
            @Context UriInfo uinfo,
            @Context HttpServletRequest req) {
        CookieBaker login = loginCookieFactory.create();
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/").build())
                .cookie(login.toDiscardingCookie())
                .build();
    }
}
