package app.controllers;

import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class IndexController {
    @GET
    @Path("session")
    public Response session(@Context HttpSession session) {
        String msg = (String) session.getAttribute("msg");
        if (null == msg)
            msg = "";
        session.setAttribute("msg", "timestamp: " + System.currentTimeMillis());
        return Response.ok(msg).build();
    }
}
