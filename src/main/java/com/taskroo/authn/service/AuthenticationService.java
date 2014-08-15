package com.taskroo.authn.service;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.taskroo.authn.data.NonExistingResourceOperationException;
import com.taskroo.authn.data.RememberMeTokenDao;
import com.taskroo.authn.data.SecurityTokenDao;
import com.taskroo.authn.data.UserDao;
import com.taskroo.authn.domain.RememberMeToken;
import com.taskroo.authn.domain.SecurityToken;
import com.taskroo.authn.domain.User;
import com.taskroo.authn.domain.UserCredentials;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

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
@Api(value = "authToken", description = "Security token management")
public class AuthenticationService {

    private final SecurityTokenDao securityTokenDao;
    private final UserDao userDao;
    private final RememberMeTokenDao rememberMeTokenDao;

    private static final Logger LOGGER = LogManager.getLogger();

    @Inject
    public AuthenticationService(SecurityTokenDao securityTokenDao, UserDao userDao, RememberMeTokenDao rememberMeTokenDao) {
        this.securityTokenDao = securityTokenDao;
        this.userDao = userDao;
        this.rememberMeTokenDao = rememberMeTokenDao;
    }

    @Path("/login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create security token for the user", notes = "Returns security token containing tokenId required for authorization", response = SecurityToken.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "security token created correctly"),
            @ApiResponse(code = 401, message = "user with given login and password not exists")})
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

        SecurityToken securityToken;
        if (credentials.isRememberMe()) {
            securityToken = SecurityToken.createWithRememberMeToken(user);
            rememberMeTokenDao.saveToken(new RememberMeToken(user.getUsername(), securityToken.getRememberMeToken().split(":")[1]));
        } else {
            securityToken = SecurityToken.create(user);
        }
        securityTokenDao.insert(securityToken);
        LOGGER.debug("Authentication token created for user {}", credentials.getUsername());
        return Response.created(URI.create("authToken/" + securityToken.getId())).entity(securityToken).build();
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

    @Path("/loginWithRememberMe")
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create security token for the user", notes = "Create security token for user using rememberMeToken", response = SecurityToken.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "security token created correctly"),
            @ApiResponse(code = 400, message = "invalid format of given rememberMe token"),
            @ApiResponse(code = 401, message = "invalid rememberMe token given")})
    public Response createSecurityTokenWithRememberMeToken(String rememberMeTokenString) {
        try {
            RememberMeToken rememberMeToken = RememberMeToken.fromString(rememberMeTokenString);

            User user = userDao.findByUsername(rememberMeToken.getUsername());
            if (!rememberMeTokenDao.tokenExists(rememberMeToken)) {
                LOGGER.info("Incorrect rememberMeToken received for user: {}", rememberMeToken.getUsername());
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            rememberMeTokenDao.remove(rememberMeToken);
            SecurityToken securityToken = SecurityToken.createWithRememberMeToken(user);
            securityTokenDao.insert(securityToken);
            rememberMeTokenDao.saveToken(new RememberMeToken(user.getUsername(), securityToken.getRememberMeToken().split(":")[1]));

            return Response.created(URI.create("authToken/" + securityToken.getId())).entity(securityToken).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid token").build();
        }
    }

    @DELETE
    @Path("/{securityTokenId}")
    @ApiOperation(value = "Delete security token")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "security token deleted"),
            @ApiResponse(code = 404, message = "security token with given id not found")})
    public Response logout(@PathParam("securityTokenId") String securityTokenId) {
        try {
            SecurityToken securityToken = securityTokenDao.findById(securityTokenId);
            if (securityToken.getRememberMeToken() != null) {
                RememberMeToken rememberMeToken = RememberMeToken.fromString(securityToken.getRememberMeToken());
                rememberMeTokenDao.remove(rememberMeToken);
            }
            securityTokenDao.remove(securityTokenId);
            return Response.noContent().build();
        } catch (NonExistingResourceOperationException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
