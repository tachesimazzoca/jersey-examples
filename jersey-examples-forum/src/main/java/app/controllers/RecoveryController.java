package app.controllers;

import app.core.inject.UserContext;
import app.core.mail.MailerException;
import app.core.mail.TextMailerFactory;
import app.core.storage.Storage;
import app.core.view.FormHelper;
import app.core.view.View;
import app.models.Account;
import app.models.AccountDao;
import app.models.RecoveryEntryForm;
import app.models.RecoveryResetForm;
import app.models.UserHelper;
import com.google.common.base.Optional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Map;
import java.util.Set;

import static app.core.util.ParameterUtils.params;

@Path("/recovery")
@Produces(MediaType.TEXT_HTML)
public class RecoveryController {
    private final Validator validator;
    private final AccountDao accountDao;
    private final Storage<Map<String, Object>> recoveryStorage;
    private final TextMailerFactory recoveryMailerFactory;

    public RecoveryController(
            Validator validator,
            AccountDao accountDao,
            Storage<Map<String, Object>> recoveryStorage,
            TextMailerFactory recoveryMailerFactory) {
        this.validator = validator;
        this.accountDao = accountDao;
        this.recoveryStorage = recoveryStorage;
        this.recoveryMailerFactory = recoveryMailerFactory;
    }

    @GET
    @Path("errors/{name}")
    public Response errors(@PathParam("name") String name) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new View("recovery/errors/" + name))
                .build();
    }

    @GET
    @Path("entry")
    public Response entry(@UserContext UserHelper userHelper) {
        userHelper.logout();
        RecoveryEntryForm form = RecoveryEntryForm.defaultForm();
        View view = new View("recovery/entry", params(
                "form", new FormHelper<RecoveryEntryForm>(form)));
        return Response.ok(view).cookie(userHelper.toCookie()).build();
    }

    @POST
    @Path("entry")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEntry(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            MultivaluedMap<String, String> formParams)
            throws MailerException {

        userHelper.logout();

        RecoveryEntryForm form = RecoveryEntryForm.bindFrom(formParams);
        Account account = null;
        if (validator.validateProperty(form, "email").isEmpty()) {
            Optional<Account> accountOpt = accountDao.findByEmail(form.getEmail());
            if (accountOpt.isPresent()) {
                account = accountOpt.get();
            } else {
                form.setActiveEmail(false);
            }
        }
        Set<ConstraintViolation<RecoveryEntryForm>> errors = validator.validate(form);
        if (account == null) {
            View view = new View("recovery/entry", params(
                    "form", new FormHelper<RecoveryEntryForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .cookie(userHelper.toCookie()).build();
        }

        Map<String, Object> params = params("id", account.getId());
        String code = recoveryStorage.create(params);
        String url = uriInfo.getBaseUriBuilder()
                .path("/recovery/reset")
                .queryParam("code", code)
                .build()
                .toString();
        recoveryMailerFactory.create(form.getEmail(), url).send();

        return Response.ok(new View("recovery/verify"))
                .cookie(userHelper.toCookie()).build();
    }

    @GET
    @Path("reset")
    public Response reset(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("code") String code) {

        userHelper.logout();

        Optional<Map<String, Object>> opt = recoveryStorage.read(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build())
                    .cookie(userHelper.toCookie()).build();
        }
        RecoveryResetForm form = RecoveryResetForm.defaultForm();
        form.setCode(code);
        View view = new View("recovery/reset", params(
                "form", new FormHelper<RecoveryResetForm>(form)));
        return Response.ok(view).cookie(userHelper.toCookie()).build();
    }

    @POST
    @Path("reset")
    @Consumes("application/x-www-form-urlencoded")
    public Response postReset(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            MultivaluedMap<String, String> formParams) {

        userHelper.logout();

        RecoveryResetForm form = RecoveryResetForm.bindFrom(formParams);

        Optional<Map<String, Object>> opt = recoveryStorage.read(form.getCode());
        if (!opt.isPresent()) {
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build())
                    .cookie(userHelper.toCookie()).build();
        }
        Map<String, Object> params = opt.get();

        Long id = (Long) params.get("id");
        Optional<Account> accountOpt = accountDao.find(id);
        if (!accountOpt.isPresent()) {
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build())
                    .cookie(userHelper.toCookie()).build();
        }
        Account account = accountOpt.get();

        Set<ConstraintViolation<RecoveryResetForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("recovery/reset", params(
                    "form", new FormHelper<RecoveryResetForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .cookie(userHelper.toCookie()).build();
        }

        account.refreshPassword(form.getPassword());
        accountDao.save(account);

        recoveryStorage.delete(form.getCode());

        return Response.ok(new View("recovery/complete"))
                .cookie(userHelper.toCookie()).build();
    }
}
