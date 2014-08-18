package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import app.core.View;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PagesController {
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
        return Response.ok(new View("pages/" + name)).build();
    }
}
