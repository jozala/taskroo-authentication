package com.taskroo.authn.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;

public class JettyRunner {

    private static Logger LOGGER = LogManager.getLogger();

    private static Server server;

    public static void main(String[] args) {
        startIfNotRunning("http://localhost/", 8081);
    }

    public static void startIfNotRunning(String hostname, int port) {
        if (isJettyRunningOnPort(port)) {
            LOGGER.info("Jetty is already running on port {}", port);
            return;
        }
        LOGGER.info("Starting Jetty on port {}", port);
        URI baseUri = UriBuilder.fromUri(hostname).port(port).build();
        ResourceConfig config = new AuthnServiceApp();
        server = JettyHttpContainerFactory.createServer(baseUri, config);
        try {
            server.start();
        } catch (Exception e) {
            LOGGER.fatal("Unable to start Jetty on host {} and port {}", hostname, port);
            throw new RuntimeException("Could not start a jetty instance: " + e.getMessage(), e);
        }
    }

    private static boolean isJettyRunningOnPort(int port) {
        try{
            new Socket("localhost", port);
        }
        catch(IOException e) {
            return false;
        }
        return true;
    }

    public static void stop() {
        if (server == null) {
            LOGGER.info("Jetty server has not been started so it will not be stopped");
            return;
        }
        try {
            server.stop();
            LOGGER.info("Jetty server stopped");
        } catch (Exception e) {
            throw new RuntimeException("Could not stop a jetty instance: " + e.getMessage(), e);
        }
    }
}
