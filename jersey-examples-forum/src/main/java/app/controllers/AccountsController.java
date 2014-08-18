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

import org.apache.commons.mail.EmailException;

import app.core.*;
import app.mail.TextMailerFactory;
import app.models.*;

import static app.core.Util.params;
import static app.core.Util.safeURI;

@Path("/accounts")
@Produces(MediaType.TEXT_HTML)
public class AccountsController {
    private final Validator validator;
    private final AccountDao accountDao;
    private final Storage signupStorage;
    private final TextMailerFactory signupMailerFactory;

    public AccountsController(
            AccountDao accountDao,
            Storage signupStorage,
            TextMailerFactory signupMailerFactory) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
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
        if (!form.getEmail().isEmpty()) {
            if (accountDao.findByEmail(form.getEmail()).isPresent()) {
                form.setUniqueEmail(false);
            }
        }
        Set<ConstraintViolation<AccountsSignupForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("accounts/signup", params(
                    "form", new FormHelper<AccountsSignupForm>(form, errors)));
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
    public Response signin(
            @Context Session session,
            @QueryParam("returnTo") @DefaultValue("") String returnTo) {
        session.remove("accountId");
        AccountsSigninForm form = AccountsSigninForm.defaultForm();
        form.setReturnTo(returnTo);
        return Response.ok(new View("accounts/signin", params(
                "form", new FormHelper<AccountsSigninForm>(form))))
                .cookie(session.toCookie()).build();
    }

    @POST
    @Path("signin")
    @Consumes("application/x-www-form-urlencoded")
    public Response postSignin(
            @Context Session session,
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {

        AccountsSigninForm form = AccountsSigninForm.bindFrom(formParams);

        Set<ConstraintViolation<AccountsSigninForm>> errors = validator.validate(form);
        Account account = null;
        if (errors.isEmpty()) {
            Optional<Account> accountOpt = accountDao.findByEmail(form.getEmail());
            if (accountOpt.isPresent() && accountOpt.get().isEqualPassword(form.getPassword())) {
                account = accountOpt.get();
            } else {
                form.setAuthorized(false);
                errors = validator.validate(form);
            }
        }
        if (account == null) {
            View view = new View("accounts/signin", params(
                    "form", new FormHelper<AccountsSigninForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        session.put("accountId", account.getId().toString());

        String returnTo = form.getReturnTo();
        if (!returnTo.startsWith("/") || returnTo.isEmpty())
            returnTo = "/";
        return Response.seeOther(safeURI(uinfo, returnTo)).cookie(session.toCookie()).build();
    }

    @GET
    @Path("signout")
    public Response signout(
            @Context Session session,
            @Context UriInfo uinfo) {
        session.remove("accountId");
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/").build())
                .cookie(session.toCookie())
                .build();
    }
}
