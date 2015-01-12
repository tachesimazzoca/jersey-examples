package app.controllers;

import app.core.inject.UserContext;
import app.core.view.View;
import app.models.ForumUser;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static app.core.util.ParameterUtils.params;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class PagesController {
    @GET
    public Response home(@UserContext ForumUser forumUser) {
        return index(forumUser);
    }

    @GET
    @Path("index.html")
    public Response index(@UserContext ForumUser forumUser) {
        return page(forumUser, "index");
    }

    @GET
    @Path("pages/{name}.html")
    public Response page(
            @UserContext ForumUser forumUser,
            @PathParam("name") String name) {
        View view = new View("pages/" + name, params(
                "account", forumUser.getAccount().orNull()));
        return Response.ok(view).build();
    }
}
