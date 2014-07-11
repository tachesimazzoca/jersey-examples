package app.controllers;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import app.core.Page;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PagesController {
    private final Map<String, Object> params;

    public PagesController() {
        this(ImmutableMap.<String, Object> of());
    }

    public PagesController(ImmutableMap<String, Object> params) {
        this.params = params;
    }

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
        return Response.ok(new Page("pages/" + name, params)).build();
    }
}
