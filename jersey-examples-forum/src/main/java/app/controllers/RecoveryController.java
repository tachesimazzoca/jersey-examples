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

@Path("/recovery")
@Produces(MediaType.TEXT_HTML)
public class RecoveryController {
    private final Validator validator;
    private final AccountDao accountDao;
    private final Storage<Map<String, Object>> recoveryStorage;
    private final TextMailerFactory recoveryMailerFactory;

    public RecoveryController(
            AccountDao accountDao,
            Storage<Map<String, Object>> recoveryStorage,
            TextMailerFactory recoveryMailerFactory) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
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
    public Response entry(@Context UserContext userContext) {
        userContext.logout();
        RecoveryEntryForm form = RecoveryEntryForm.defaultForm();
        View view = new View("recovery/entry", params(
                "form", new FormHelper<RecoveryEntryForm>(form)));
        return Response.ok(view).cookie(userContext.toCookie()).build();
    }

    @POST
    @Path("entry")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEntry(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams)
            throws EmailException {

        userContext.logout();

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
                    .cookie(userContext.toCookie()).build();
        }

        Map<String, Object> params = params("id", account.getId());
        String code = recoveryStorage.create(params);
        String url = uinfo.getBaseUriBuilder()
                .path("/recovery/reset")
                .queryParam("code", code)
                .build()
                .toString();
        recoveryMailerFactory.create(form.getEmail(), url).send();

        return Response.ok(new View("recovery/verify"))
                .cookie(userContext.toCookie()).build();
    }

    @GET
    @Path("reset")
    public Response reset(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            @QueryParam("code") String code) {

        userContext.logout();

        Optional<Map<String, Object>> opt = recoveryStorage.read(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build())
                    .cookie(userContext.toCookie()).build();
        }
        RecoveryResetForm form = RecoveryResetForm.defaultForm();
        form.setCode(code);
        View view = new View("recovery/reset", params(
                "form", new FormHelper<RecoveryResetForm>(form)));
        return Response.ok(view).cookie(userContext.toCookie()).build();
    }

    @POST
    @Path("reset")
    @Consumes("application/x-www-form-urlencoded")
    public Response postReset(
            @Context UserContext userContext,
            @Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {

        userContext.logout();

        RecoveryResetForm form = RecoveryResetForm.bindFrom(formParams);

        Optional<Map<String, Object>> opt = recoveryStorage.read(form.getCode());
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build())
                    .cookie(userContext.toCookie()).build();
        }
        Map<String, Object> params = opt.get();

        Long id = (Long) params.get("id");
        Optional<Account> accountOpt = accountDao.find(id);
        if (!accountOpt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build())
                    .cookie(userContext.toCookie()).build();
        }
        Account account = accountOpt.get();

        Set<ConstraintViolation<RecoveryResetForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("recovery/reset", params(
                    "form", new FormHelper<RecoveryResetForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .cookie(userContext.toCookie()).build();
        }

        account.refreshPassword(form.getPassword());
        accountDao.save(account);

        recoveryStorage.delete(form.getCode());

        return Response.ok(new View("recovery/complete"))
                .cookie(userContext.toCookie()).build();
    }
}
