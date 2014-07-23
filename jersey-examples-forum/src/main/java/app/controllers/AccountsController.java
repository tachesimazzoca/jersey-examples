package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilderException;
import javax.ws.rs.core.UriInfo;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.mail.EmailException;

import app.core.*;
import app.mail.TextMailerFactory;
import app.models.*;

import static app.core.Util.params;

@Path("/accounts")
@Produces(MediaType.TEXT_HTML)
public class AccountsController {
    private final Validator validator;
    private final CookieBakerFactory loginCookieFactory;
    private final AccountDao accountDao;
    private final Storage signupStorage;
    private final TextMailerFactory signupMailerFactory;

    public AccountsController(
            CookieBakerFactory loginCookieFactory,
            AccountDao accountDao,
            Storage signupStorage,
            TextMailerFactory signupMailerFactory) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.loginCookieFactory = loginCookieFactory;
        this.accountDao = accountDao;
        this.signupStorage = signupStorage;
        this.signupMailerFactory = signupMailerFactory;
    }

    @GET
    @Path("errors/{name}")
    public Response errors(@PathParam("name") String name) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new View("accounts/errors/" + name))
                .build();
    }

    @GET
    @Path("signup")
    public Response signup() {
        View view = new View("accounts/signup", params(
                "form", new FormHelper<AccountsSignupForm>(AccountsSignupForm.defaultForm())));
        return Response.ok(view).build();
    }

    @POST
    @Path("signup")
    @Consumes("application/x-www-form-urlencoded")
    public Response postSignup(@Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams)
            throws EmailException {
        AccountsSignupForm form = AccountsSignupForm.bindFrom(formParams);
        Set<ConstraintViolation<AccountsSignupForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("accounts/signup", params(
                    "form", new FormHelper<AccountsSignupForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }
        if (accountDao.findByEmail(form.getEmail()).isPresent()) {
            List<String> messages = ImmutableList.of("The email has already been used.");
            View view = new View("accounts/signup", params(
                    "form", new FormHelper<AccountsSignupForm>(form, messages)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }

        Map<String, Object> params = params(
                "email", form.getEmail(),
                "password", form.getPassword());
        String code = signupStorage.create(params);
        String url = uinfo.getBaseUriBuilder()
                .path("/accounts/activate")
                .queryParam("code", code)
                .build()
                .toString();
        signupMailerFactory.create(form.getEmail(), url).send();

        return Response.ok(new View("accounts/verify")).build();
    }

    @GET
    @Path("activate")
    public Response activate(@Context UriInfo uinfo, @QueryParam("code") String code) {
        Optional<?> opt = signupStorage.read(code, Map.class);
        signupStorage.delete(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/accounts/errors/session").build()).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) opt.get();
        if (accountDao.findByEmail(params.get("email")).isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/accounts/errors/email").build()).build();
        }

        Account account = new Account();
        account.setEmail(params.get("email"));
        account.setStatus(Account.Status.ACTIVE);
        account.refreshPassword(params.get("password"));
        Account savedAccount = accountDao.save(account);
        return Response.ok(new View("accounts/activate", params("account", savedAccount))).build();
    }

    @GET
    @Path("signin")
    public Response signin(@QueryParam("url") @DefaultValue("") String url) {
        AccountsSigninForm form = AccountsSigninForm.defaultForm();
        form.setUrl(url);
        CookieBaker login = loginCookieFactory.create();
        return Response.ok(new View("accounts/signin", params(
                "form", new FormHelper<AccountsSigninForm>(form))))
                .cookie(login.toDiscardingCookie()).build();
    }

    @POST
    @Path("signin")
    @Consumes("application/x-www-form-urlencoded")
    public Response postSignin(
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {

        AccountsSigninForm form = AccountsSigninForm.bindFrom(formParams);
        Set<ConstraintViolation<AccountsSigninForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("accounts/signin", params(
                    "form", new FormHelper<AccountsSigninForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        Optional<Account> accountOpt = accountDao.findByEmail(form.getEmail());
        if (!accountOpt.isPresent() || !accountOpt.get().isEqualPassword(form.getPassword())) {
            List<String> messages = ImmutableList.of("Invalid password or e-mail");
            View view = new View("accounts/signin", params(
                    "form", new FormHelper<AccountsSigninForm>(form, messages)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }
        Account account = accountOpt.get();

        CookieBaker login = loginCookieFactory.create();
        login.put("id", account.getId().toString());

        String url = form.getUrl();
        if (!url.startsWith("/") || url.isEmpty())
            url = "/";
        try {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .uri(new URI(url)).build()).cookie(login.toCookie()).build();
        } catch (UriBuilderException e) {
            throw new IllegalArgumentException(e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @GET
    @Path("signout")
    public Response signout(@Context UriInfo uinfo) {
        CookieBaker login = loginCookieFactory.create();
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/").build())
                .cookie(login.toDiscardingCookie())
                .build();
    }
}
