package pl.aetas.gtweb.authn.server;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class AuthnServiceApp extends ResourceConfig {
    public AuthnServiceApp() {
        register(JacksonFeature.class);
        register(ExceptionListener.class);
        register(RolesAllowedDynamicFeature.class);
        packages("pl.aetas.gtweb.authn.service");
    }
}
