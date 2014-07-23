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
import com.google.common.collect.ImmutableList;

import java.util.List;
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
    private final Storage recoveryStorage;
    private final TextMailerFactory recoveryMailerFactory;

    public RecoveryController(
            AccountDao accountDao,
            Storage recoveryStorage,
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
    public Response entry() {
        RecoveryEntryForm form = RecoveryEntryForm.defaultForm();
        View view = new View("recovery/entry", params(
                "form", new FormHelper<RecoveryEntryForm>(form)));
        return Response.ok(view).build();
    }

    @POST
    @Path("entry")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEntry(@Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams)
            throws EmailException {
        RecoveryEntryForm form = RecoveryEntryForm.bindFrom(formParams);
        Set<ConstraintViolation<RecoveryEntryForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("recovery/entry", params(
                    "form", new FormHelper<RecoveryEntryForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }
        Optional<Account> accountOpt = accountDao.findByEmail(form.getEmail());
        if (!accountOpt.isPresent()) {
            List<String> messages = ImmutableList.of("The e-mail address does not exist.");
            View view = new View("recovery/entry", params(
                    "form", new FormHelper<RecoveryEntryForm>(form, messages)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }

        Map<String, Object> params = params("id", accountOpt.get().getId());
        String code = recoveryStorage.create(params);
        String url = uinfo.getBaseUriBuilder()
                .path("/recovery/reset")
                .queryParam("code", code)
                .build()
                .toString();
        recoveryMailerFactory.create(form.getEmail(), url).send();

        return Response.ok(new View("recovery/verify")).build();
    }

    @GET
    @Path("reset")
    public Response reset(@Context UriInfo uinfo, @QueryParam("code") String code) {
        Optional<?> opt = recoveryStorage.read(code, Map.class);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build()).build();
        }
        RecoveryResetForm form = RecoveryResetForm.defaultForm();
        form.setCode(code);
        View view = new View("recovery/reset", params(
                "form", new FormHelper<RecoveryResetForm>(form)));
        return Response.ok(view).build();
    }

    @POST
    @Path("reset")
    @Consumes("application/x-www-form-urlencoded")
    public Response postReset(@Context UriInfo uinfo,
            MultivaluedMap<String, String> formParams) {
        RecoveryResetForm form = RecoveryResetForm.bindFrom(formParams);

        Optional<?> opt = recoveryStorage.read(form.getCode(), Map.class);
        if (!opt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build()).build();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) opt.get();

        Long id = (Long) params.get("id");
        Optional<Account> accountOpt = accountDao.find(id);
        if (!accountOpt.isPresent()) {
            return Response.seeOther(uinfo.getBaseUriBuilder()
                    .path("/recovery/errors/session").build()).build();
        }
        Account account = accountOpt.get();

        Set<ConstraintViolation<RecoveryResetForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("recovery/reset", params(
                    "form", new FormHelper<RecoveryResetForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }

        account.refreshPassword(form.getPassword());
        accountDao.save(account);

        recoveryStorage.delete(form.getCode());

        return Response.ok(new View("recovery/complete")).build();
    }
}
