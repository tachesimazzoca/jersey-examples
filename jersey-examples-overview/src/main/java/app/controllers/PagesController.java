package app.controllers;

import app.core.config.Config;
import app.core.view.View;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static app.core.util.ParameterUtils.params;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PagesController {
    @Inject Config config;

    @GET
    public Response home() {
        return index();
    }

    @GET
    @Path("index.html")
    public Response index() {
        return page("index");
    }

    @GET
    @Path("pages/{name}.html")
    public Response page(@PathParam("name") String name) {
        View view = new View("pages/" + name, params("message", "Hello PagesController!"));
        return Response.ok(view).build();
    }
}
