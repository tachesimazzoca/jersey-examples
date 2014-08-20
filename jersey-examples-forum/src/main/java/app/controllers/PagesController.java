package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import app.core.View;
import app.models.UserContext;

import static app.core.Util.params;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PagesController {
    @GET
    public Response home(@Context UserContext userContext) {
        return index(userContext);
    }

    @GET
    @Path("index.html")
    public Response index(@Context UserContext userContext) {
        return page(userContext, "index");
    }

    @GET
    @Path("pages/{name}.html")
    public Response page(
            @Context UserContext userContext,
            @PathParam("name") String name) {
        View view = new View("pages/" + name, params(
                "account", userContext.getAccount().orNull()));
        return Response.ok(view).build();
    }
}
