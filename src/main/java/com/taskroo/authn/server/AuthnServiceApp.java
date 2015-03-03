package com.taskroo.authn.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class AuthnServiceApp extends ResourceConfig {
    public AuthnServiceApp() {
        register(ExceptionListener.class);
        register(RolesAllowedDynamicFeature.class);
        register(CORSResponseFilter.class);
        packages("com.taskroo.authn.service", "com.wordnik.swagger.jersey.listing");
    }
}
