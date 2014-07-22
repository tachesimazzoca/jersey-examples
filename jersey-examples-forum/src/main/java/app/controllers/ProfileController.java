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
    private final UserDao userDao;

    public ProfileController(
            CookieBakerFactory loginCookieFactory,
            UserDao userDao) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.loginCookieFactory = loginCookieFactory;
        this.userDao = userDao;
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
        Optional<User> userOpt = userDao.find(Long.parseLong(login.get("id").get()));
        if (!userOpt.isPresent()) {
            return redirect;
        }

        User user = userOpt.get();
        ProfileEditForm form = ProfileEditForm.defaultForm();
        form.setEmail(user.getEmail());

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
        Optional<User> userOpt = userDao.find(Long.parseLong(login.get("id").get()));
        if (!userOpt.isPresent()) {
            return redirect;
        }
        User user = userOpt.get();

        ProfileEditForm form = ProfileEditForm.bindFrom(formParams);
        Set<ConstraintViolation<ProfileEditForm>> errors = validator.validate(form);
        if (!errors.isEmpty()) {
            View view = new View("profile/edit", params(
                    "form", new FormHelper<ProfileEditForm>(form, errors)));
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(view).build();
        }

        if (!form.getCurrentPassword().isEmpty()) {
            if (!user.isEqualPassword(form.getCurrentPassword())) {
                List<String> messages = ImmutableList.<String> of("Invalid password");
                View view = new View("profile/edit", params(
                        "form", new FormHelper<ProfileEditForm>(form, messages)));
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(view).build();
            }
        }

        if (!form.getEmail().equals(user.getEmail())) {
            if (userDao.findByEmail(form.getEmail()).isPresent()) {
                List<String> messages = ImmutableList.of("The email has already been used.");
                View view = new View("profile/edit", params(
                        "form", new FormHelper<ProfileEditForm>(form, messages)));
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(view).build();
            }
        }

        user.setEmail(form.getEmail());
        if (!form.getPassword().isEmpty()) {
            user.refreshPassword(form.getPassword());
        }
        userDao.save(user);

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
