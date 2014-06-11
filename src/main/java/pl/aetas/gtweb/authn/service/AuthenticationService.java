package pl.aetas.gtweb.authn.service;

import org.springframework.stereotype.Component;
import pl.aetas.gtweb.authn.data.SessionDao;
import pl.aetas.gtweb.authn.data.UserDao;
import pl.aetas.gtweb.authn.domain.Session;
import pl.aetas.gtweb.authn.domain.User;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;


@Component
@Path("authToken")
public class AuthenticationService {

    private final SessionDao sessionDao;
    private final UserDao userDao;

    @Inject
    public AuthenticationService(SessionDao sessionDao, UserDao userDao) {
        this.sessionDao = sessionDao;
        this.userDao = userDao;
    }

    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("username") String username, @FormParam("password") String password) {
        // TODO encode password before looking in the db
        // TODO it is probably better to prepare service responsible for user login checks
        User user = userDao.findEnabled(username, password);
        if (user == null) {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
        }
        Session session = sessionDao.create(username);
        return Response.created(URI.create("authToken/" + session.getSessionId())).entity(session).build();
    }

    @DELETE
    @Path("{sessionId}")
    public Response logout(@PathParam("sessionId") String sessionId) {
        sessionDao.remove(sessionId);
        return Response.noContent().build();
    }
}
