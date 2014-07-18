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

import java.util.Set;

import app.core.*;
import app.models.*;
import static app.core.Util.params;

@Path("/auth")
@Produces(MediaType.TEXT_HTML)
public class AuthController {
    private final Config config;
    private final Validator validator;
    private final CookieBakerFactory sessionFactory;

    public AuthController(
            Config config,
            CookieBakerFactory sessionFactory) {
        this.config = config;
        this.sessionFactory = sessionFactory;
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @GET
    @Path("login")
    public Response login(
            @Context HttpServletRequest req,
            @QueryParam("url") @DefaultValue("") String url) {
        AuthLoginForm form = AuthLoginForm.defaultForm();
        form.setUrl(url);
        CookieBaker sess = sessionFactory.create(req).clear();
        return Response.ok(new View("auth/login", params(
                "form", new FormHelper<AuthLoginForm>(form))))
                .cookie(sess.toCookie()).build();
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
            return Response.status(Response.Status.FORBIDDEN).entity(view)
                    .build();
        }

        // TODO: create login session
        CookieBaker sess = sessionFactory.create(req).clear();

        String url = form.getUrl();
        if (!url.startsWith("/") || url.isEmpty())
            url = (String) config.get("url.home");
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path(url).build()).cookie(sess.toCookie()).build();
    }

    @GET
    @Path("logout")
    public Response logout(
            @Context UriInfo uinfo,
            @Context HttpServletRequest req) {
        CookieBaker sess = sessionFactory.create(req).clear();
        return Response.seeOther(uinfo.getBaseUriBuilder()
                .path("/").build()).cookie(sess.toCookie()).build();
    }
}
