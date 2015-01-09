package app.resources;

import app.core.config.Config;
import app.core.http.Session;
import app.core.http.StorageSession;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
@Produces("text/plain")
public class MainResource {
    @Context Config config;

    private final String message;

    public MainResource(String message) {
        this.message = message;
    }

    @GET
    @Path("/hello")
    public Response hello() {
        return Response.status(Response.Status.OK).entity(message).build();
    }

    @GET
    @Path("/baseURI")
    public Response baseURI(@Context UriInfo uriInfo) {
        String uri = uriInfo.getBaseUriBuilder().build().toString();
        return Response.ok(uri).build();
    }

    @GET
    @Path("/config")
    public Response config() {
        return Response.ok(config.toString()).build();
    }

    @GET
    @Path("/session")
    public Response session(@Session StorageSession session) {
        return Response.ok(session.toString())
                .cookie(session.toCookie()).build();
    }
}
