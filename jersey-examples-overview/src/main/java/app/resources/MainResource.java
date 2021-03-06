package app.resources;

import app.core.config.Config;
import app.core.inject.UserContext;
import app.core.session.CookieSession;
import app.core.session.StorageSession;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Produces("text/plain")
public class MainResource {
    @Context
    Config config;

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
    public Response baseURI(@Context final UriInfo uriInfo) {
        String uri = uriInfo.getBaseUriBuilder().build().toString();
        return Response.ok(uri).build();
    }

    @GET
    @Path("/config")
    public Response config() {
        return Response.ok(config.toString()).build();
    }

    @GET
    @Path("/storageSession")
    public Response storageSession(
            @UserContext final StorageSession session,
            @QueryParam("save") final String save,
            @QueryParam("flash") final String flash) {

        String lastSave = session.get("save").or("");
        if (null != save)
            session.put("save", save);

        String lastFlash = session.remove("flash").or("");
        if (null != flash)
            session.put("flash", flash);

        return Response.ok(String.format("%s :save = %s, :flash = %s",
                session.toString(), lastSave, lastFlash))
                .cookie(session.toCookie()).build();
    }

    @GET
    @Path("/cookieSession")
    public Response cookieSession(
            @UserContext CookieSession session,
            @QueryParam("save") final String save,
            @QueryParam("flash") final String flash) {

        String lastSave = session.get("save").or("");
        if (null != save)
            session.put("save", save);

        String lastFlash = session.remove("flash").or("");
        if (null != flash)
            session.put("flash", flash);

        return Response.ok(String.format("%s :save = %s, :flash = %s",
                session.toString(), lastSave, lastFlash))
                .cookie(session.toCookie()).build();
    }
}
