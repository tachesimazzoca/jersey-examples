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
    public Response signup(@Context UserContext userContext) {
        userContext.logout();
        View view = new View("accounts/signup", params(
                "form", new FormHelper<AccountsSignupForm>(AccountsSignupForm.defaultForm())));
        return Response.ok(view).cookie(userContext.toCookie()).build();
    }

    @POST
    @Path("signup")
    @Consumes("application/x-www-form-urlencoded")
    public Response postSignup(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams)
            throws EmailException {

        userContext.logout();

        AccountsSignupForm form = AccountsSignupForm.bindFrom(formParams);
        if (validator.validateProperty(form, "email").isEmpty()) {
            if (!form.getEmail().isEmpty()) {
                if (accountDao.findByEmail(form.getEmail()).isPresent()) {
                    form.setUniqueEmail(false);
                }
            }
        }
        Set<ConstraintViolation<AccountsSignupForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("accounts/signup", params(
                    "form", new FormHelper<AccountsSignupForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .cookie(userContext.toCookie()).build();
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

        return Response.ok(new View("accounts/verify"))
                .cookie(userContext.toCookie()).build();
    }

    @GET
    @Path("activate")
    public Response activate(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @QueryParam("code") String code) {

        userContext.logout();

        Optional<?> opt = signupStorage.read(code, Map.class);
        signupStorage.delete(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/accounts/errors/session").build())
                    .cookie(userContext.toCookie()).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) opt.get();
        if (accountDao.findByEmail(params.get("email")).isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/accounts/errors/email").build())
                    .cookie(userContext.toCookie()).build();
        }

        Account account = new Account();
        account.setEmail(params.get("email"));
        account.setStatus(Account.Status.ACTIVE);
        account.refreshPassword(params.get("password"));
        Account savedAccount = accountDao.save(account);
        return Response.ok(new View("accounts/activate",
                params("account", savedAccount)))
                .cookie(userContext.toCookie()).build();
    }

    @GET
    @Path("signin")
    public Response signin(
            @Context UserContext userContext,
            @QueryParam("returnTo") @DefaultValue("") String returnTo) {

        userContext.logout();

        AccountsSigninForm form = AccountsSigninForm.defaultForm();
        form.setReturnTo(returnTo);
        return Response.ok(new View("accounts/signin", params(
                "form", new FormHelper<AccountsSigninForm>(form))))
                .cookie(userContext.toCookie()).build();
    }

    @POST
    @Path("signin")
    @Consumes("application/x-www-form-urlencoded")
    public Response postSignin(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {

        userContext.logout();

        AccountsSigninForm form = AccountsSigninForm.bindFrom(formParams);
        Set<ConstraintViolation<AccountsSigninForm>> errors = validator.validate(form);

        Account account = null;
        if (errors.isEmpty()) {
            Optional<Account> accountOpt = userContext.authenticate(
                    form.getEmail(), form.getPassword());
            if (accountOpt.isPresent()) {
                account = accountOpt.get();
            } else {
                form.setAuthorized(false);
                errors = validator.validate(form);
            }
        }
        if (account == null) {
            View view = new View("accounts/signin", params(
                    "form", new FormHelper<AccountsSigninForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .cookie(userContext.toCookie()).build();
        }

        String returnTo = form.getReturnTo();
        if (!returnTo.startsWith("/") || returnTo.isEmpty())
            returnTo = "/";
        return Response.seeOther(safeURI(uinfo, returnTo))
                .cookie(userContext.toCookie()).build();
    }

    @GET
    @Path("signout")
    public Response signout(
            @Context UserContext userContext,
            @Context UriInfo uinfo) {
        userContext.logout();
        return Response.seeOther(uinfo.getBaseUriBuilder().path("/").build())
                .cookie(userContext.toCookie()).build();
    }
}
