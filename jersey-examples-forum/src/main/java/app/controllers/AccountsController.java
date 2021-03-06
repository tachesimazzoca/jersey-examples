package app.controllers;

import app.core.config.Config;
import app.core.inject.UserContext;
import app.core.mail.MailerException;
import app.core.mail.TextMailerFactory;
import app.core.storage.Storage;
import app.core.view.FormHelper;
import app.core.view.View;
import app.models.Account;
import app.models.AccountDao;
import app.models.AccountsSigninForm;
import app.models.AccountsSignupForm;
import app.models.UserHelper;
import com.google.common.base.Optional;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Map;
import java.util.Set;

import static app.core.util.ParameterUtils.params;
import static app.core.util.URIUtils.safeURI;

@Path("/accounts")
@Produces(MediaType.TEXT_HTML)
public class AccountsController {
    @Context
    Config config;

    private final Validator validator;
    private final AccountDao accountDao;
    private final Storage<Map<String, Object>> signupStorage;
    private final TextMailerFactory signupMailerFactory;

    public AccountsController(
            Validator validator,
            AccountDao accountDao,
            Storage<Map<String, Object>> signupStorage,
            TextMailerFactory signupMailerFactory) {
        this.validator = validator;
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
    public Response signup(@UserContext UserHelper userHelper) {
        userHelper.logout();
        View view = new View("accounts/signup", params(
                "form", new FormHelper<AccountsSignupForm>(AccountsSignupForm.defaultForm())));
        return Response.ok(view).cookie(userHelper.toCookie()).build();
    }

    @POST
    @Path("signup")
    @Consumes("application/x-www-form-urlencoded")
    public Response postSignup(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            MultivaluedMap<String, String> formParams)
            throws MailerException {

        userHelper.logout();

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
                    .cookie(userHelper.toCookie()).build();
        }

        Map<String, Object> params = params(
                "email", form.getEmail(),
                "password", form.getPassword());
        String code = signupStorage.create(params);
        String url = uriInfo.getBaseUriBuilder()
                .path("/accounts/activate")
                .queryParam("code", code)
                .build()
                .toString();
        signupMailerFactory.create(form.getEmail(), url).send();

        return Response.ok(new View("accounts/verify"))
                .cookie(userHelper.toCookie()).build();
    }

    @GET
    @Path("activate")
    public Response activate(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("code") String code) {

        userHelper.logout();

        Optional<Map<String, Object>> opt = signupStorage.read(code);
        signupStorage.delete(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/accounts/errors/session").build())
                    .cookie(userHelper.toCookie()).build();
        }

        Map<String, Object> params = opt.get();
        if (accountDao.findByEmail((String) params.get("email")).isPresent()) {
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/accounts/errors/email").build())
                    .cookie(userHelper.toCookie()).build();
        }

        Account account = new Account();
        account.setEmail((String) params.get("email"));
        account.setStatus(Account.Status.ACTIVE);
        account.refreshPassword((String) params.get("password"));
        Account savedAccount = accountDao.save(account);
        return Response.ok(new View("accounts/activate",
                params("account", savedAccount)))
                .cookie(userHelper.toCookie()).build();
    }

    @GET
    @Path("signin")
    public Response signin(
            @UserContext UserHelper userHelper,
            @QueryParam("returnTo") @DefaultValue("") String returnTo) {

        userHelper.logout();

        AccountsSigninForm form = AccountsSigninForm.defaultForm();
        form.setReturnTo(returnTo);
        return Response.ok(new View("accounts/signin", params(
                "form", new FormHelper<AccountsSigninForm>(form))))
                .cookie(userHelper.toCookie()).build();
    }

    @POST
    @Path("signin")
    @Consumes("application/x-www-form-urlencoded")
    public Response postSignin(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            MultivaluedMap<String, String> formParams) {

        userHelper.logout();

        AccountsSigninForm form = AccountsSigninForm.bindFrom(formParams);
        Set<ConstraintViolation<AccountsSigninForm>> errors = validator.validate(form);

        Account account = null;
        if (errors.isEmpty()) {
            Optional<Account> accountOpt = userHelper.authenticate(
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
                    .cookie(userHelper.toCookie()).build();
        }

        String returnTo = form.getReturnTo();
        if (!returnTo.startsWith("/") || returnTo.isEmpty())
            returnTo = config.get("url.home", String.class);

        return Response.seeOther(safeURI(uriInfo, returnTo))
                .cookie(userHelper.toCookie()).build();
    }

    @GET
    @Path("signout")
    public Response signout(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo) {
        userHelper.logout();
        String returnTo = config.get("url.home", String.class);
        return Response.seeOther(safeURI(uriInfo, returnTo))
                .cookie(userHelper.toCookie()).build();
    }
}
