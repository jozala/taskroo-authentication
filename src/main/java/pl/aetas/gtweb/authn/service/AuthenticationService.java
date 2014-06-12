package pl.aetas.gtweb.authn.service;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.aetas.gtweb.authn.data.SessionDao;
import pl.aetas.gtweb.authn.data.UserDao;
import pl.aetas.gtweb.authn.domain.Session;
import pl.aetas.gtweb.authn.domain.User;
import pl.aetas.gtweb.authn.domain.UserCredentials;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


@Component
@Path("authToken")
public class AuthenticationService {

    private final SessionDao sessionDao;
    private final UserDao userDao;

    private static final Logger LOGGER = LogManager.getLogger();

    @Inject
    public AuthenticationService(SessionDao sessionDao, UserDao userDao) {
        this.sessionDao = sessionDao;
        this.userDao = userDao;
    }

    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserCredentials credentials) {
        LOGGER.debug("Login request received for user " + credentials.getUsername());
        User user = userDao.findByUsername(credentials.getUsername());
        if (user == null) {
            LOGGER.debug("User {} not found", credentials.getUsername());
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("User not found").build());
        }

        String encodedPassword = getEncryptedPassword(credentials.getPassword(), user.getSalt());
        if (!user.getPassword().equals(encodedPassword)) {
            LOGGER.debug("Password incorrect for user {}", credentials.getUsername());
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).entity("Incorrect password").build());
        }

        Session session = sessionDao.create(credentials.getUsername());
        LOGGER.debug("Authentication token created for user {}", credentials.getUsername());
        return Response.created(URI.create("authToken/" + session.getSessionId())).entity(session).build();
    }

    private String getEncryptedPassword(String unencryptedPassword, String salt) {
        PBEKeySpec spec = new PBEKeySpec(unencryptedPassword.toCharArray(), Base64.decode(salt), 8192, 160);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return Base64.encode(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("Password encrypting with salt to compare in log-in process failed.", e);
            throw new IllegalStateException("Password encrypting with salt to compare in log-in process failed", e);
        }
    }

    @DELETE
    @Path("{sessionId}")
    public Response logout(@PathParam("sessionId") String sessionId) {
        sessionDao.remove(sessionId);
        return Response.noContent().build();
    }
}
