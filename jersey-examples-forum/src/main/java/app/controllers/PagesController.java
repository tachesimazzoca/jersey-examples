package app.controllers;

import app.core.inject.UserContext;
import app.core.view.View;
import app.models.UserHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static app.core.util.ParameterUtils.params;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PagesController {
    @GET
    public Response home(@UserContext UserHelper userHelper) {
        return index(userHelper);
    }

    @GET
    @Path("index.html")
    public Response index(@UserContext UserHelper userHelper) {
        return page(userHelper, "index");
    }

    @GET
    @Path("pages/{name}.html")
    public Response page(
            @UserContext UserHelper userHelper,
            @PathParam("name") String name) {
        View view = new View("pages/" + name, params(
                "account", userHelper.getAccount().orNull()));
        return Response.ok(view).build();
    }
}
