package app.controllers;

import app.core.inject.UserContext;
import app.core.mail.MailerException;
import app.core.mail.TextMailerFactory;
import app.core.storage.Storage;
import app.core.util.FileHelper;
import app.core.view.FormHelper;
import app.core.view.View;
import app.models.Account;
import app.models.AccountDao;
import app.models.ProfileEditForm;
import app.models.TempFileHelper;
import app.models.UserHelper;
import com.google.common.base.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static app.core.util.ParameterUtils.params;

@Path("/profile")
@Produces(MediaType.TEXT_HTML)
public class ProfileController {
    private final Validator validator;
    private final AccountDao accountDao;
    private final TempFileHelper tempFileHelper;
    private final FileHelper accountsIconFinder;
    private final Storage<Map<String, Object>> profileStorage;
    private final TextMailerFactory profileMailerFactory;

    public ProfileController(
            Validator validator,
            AccountDao accountDao,
            TempFileHelper tempFileHelper,
            FileHelper accountsIconFinder,
            Storage<Map<String, Object>> profileStorage,
            TextMailerFactory profileMailerFactory) {
        this.validator = validator;
        this.accountDao = accountDao;
        this.tempFileHelper = tempFileHelper;
        this.accountsIconFinder = accountsIconFinder;
        this.profileStorage = profileStorage;
        this.profileMailerFactory = profileMailerFactory;
    }

    @GET
    @Path("edit")
    public Response edit(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo) {

        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uriInfo);
        Account account = accountOpt.get();
        boolean icon = accountsIconFinder.find(account.getId().toString()).isPresent();

        ProfileEditForm form = ProfileEditForm.bindFrom(account);
        View view = new View("profile/edit", params(
                "account", account,
                "form", new FormHelper<ProfileEditForm>(form),
                "icon", icon,
                "flash", userHelper.getFlash().orNull()));
        return Response.ok(view).build();
    }

    @POST
    @Path("edit")
    @Consumes("application/x-www-form-urlencoded")
    public Response postEdit(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            MultivaluedMap<String, String> formParams) throws
            IOException, MailerException {

        Optional<Account> accountOpt = userHelper.getAccount();
        if (!accountOpt.isPresent())
            return redirectToLogin(uriInfo);
        Account account = accountOpt.get();
        Boolean icon = accountsIconFinder.find(account.getId().toString()).isPresent();

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
                    "account", account,
                    "icon", icon,
                    "form", new FormHelper<ProfileEditForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        account.setNickname(form.getNickname());
        if (!form.getPassword().isEmpty()) {
            account.refreshPassword(form.getPassword());
        }
        accountDao.save(account);

        if (!form.getIconToken().isEmpty()) {
            Optional<File> tempfileOpt = tempFileHelper.read(form.getIconToken());
            if (tempfileOpt.isPresent()) {
                File tempfile = tempfileOpt.get();
                String extension = FilenameUtils.getExtension(tempfile.getName());
                String iconName = account.getId().toString();
                accountsIconFinder.delete(iconName);
                accountsIconFinder.save(FileUtils.openInputStream(tempfile),
                        iconName, extension);
                FileUtils.deleteQuietly(tempfile);
            }
        }

        if (form.getEmail().equals(account.getEmail())) {
            userHelper.setFlash("saved");
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/profile/edit").build())
                    .cookie(userHelper.toCookie()).build();
        }

        Map<String, Object> params = params(
                "id", account.getId(),
                "email", form.getEmail());
        String code = profileStorage.create(params);
        String url = uriInfo.getBaseUriBuilder()
                .path("/profile/activate")
                .queryParam("code", code)
                .build()
                .toString();
        profileMailerFactory.create(form.getEmail(), url).send();

        return Response.seeOther(uriInfo.getBaseUriBuilder()
                .path("/profile/verify").build()).build();
    }

    @GET
    @Path("verify")
    public Response verify(@UserContext UserHelper userHelper) {
        return Response.ok(new View("profile/verify", params(
                "account", userHelper.getAccount().orNull()))).build();
    }

    @GET
    @Path("activate")
    public Response activate(
            @UserContext UserHelper userHelper,
            @Context UriInfo uriInfo,
            @QueryParam("code") String code) {
        Optional<Map<String, Object>> opt = profileStorage.read(code);
        profileStorage.delete(code);
        if (!opt.isPresent()) {
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/profile/errors/session").build()).build();
        }

        Map<String, Object> params = opt.get();
        Long id = (Long) params.get("id");
        String email = (String) params.get("email");

        Optional<Account> accountOpt = userHelper.getAccount();
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (!id.equals(account.getId())) {
                // The current user is not a verified user.
                return Response.seeOther(uriInfo.getBaseUriBuilder()
                        .path("/profile/errors/session").build()).build();
            }
        }

        accountOpt = accountDao.find(id);
        if (!accountOpt.isPresent()) {
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/profile/errors/session").build()).build();
        }
        if (accountDao.findByEmail(email).isPresent()) {
            return Response.seeOther(uriInfo.getBaseUriBuilder()
                    .path("/profile/errors/email").build()).build();
        }

        Account account = accountOpt.get();
        account.setEmail(email);
        Account savedAccount = accountDao.save(account);
        return Response.ok(new View("profile/activate", params(
                "account", savedAccount))).build();
    }

    @GET
    @Path("errors/{name}")
    public Response errors(
            @UserContext UserHelper userHelper,
            @PathParam("name") String name) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new View("profile/errors/" + name, params(
                        "account", userHelper.getAccount().orNull()))).build();
    }

    private Response redirectToLogin(UriInfo uriInfo) {
        return Response.seeOther(uriInfo.getBaseUriBuilder()
                .path("/accounts/signin")
                .queryParam("returnTo", "/profile/edit")
                .build()).build();
    }
}
