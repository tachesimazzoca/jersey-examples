package app.controllers;

import app.core.inject.Component;
import app.core.session.CookieSession;
import app.core.session.Session;

import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class IndexController {
    @GET
    @Path("httpSession")
    public Response httpSession(@Context HttpSession session) {
        String msg = (String) session.getAttribute("msg");
        if (null == msg)
            msg = "";
        session.setAttribute("msg", "timestamp: " + System.currentTimeMillis());
        return Response.ok(msg).build();
    }

    @GET
    @Path("cookieSession")
    public Response cookieSession(@Component Session session) {
        String msg = session.get("msg").or("");
        session.put("msg", "timestamp: " + System.currentTimeMillis());
        return Response.ok(msg).cookie(session.toCookie()).build();
    }
}
