package com.taskroo.authn.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class RememberMeToken {
    private final String username;
    private final String key;

    private static final Logger LOGGER = LogManager.getLogger();

    @JsonCreator
    public RememberMeToken(@JsonProperty("username") String username, @JsonProperty("key") String key) {
        this.username = Objects.requireNonNull(username);
        this.key = Objects.requireNonNull(key);
    }

    public static RememberMeToken createNew(String username) {
        return new RememberMeToken(username, RandomStringUtils.randomAlphanumeric(32));
    }

    public String getUsername() {
        return username;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return username + ":" + key;
    }

    @JsonIgnore
    public String getHashedKey() {

        try {
            return Base64.encode(MessageDigest.getInstance("SHA-1").digest(Base64.decode(key)));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.fatal("Cannot hash remember-me token");
            throw new InternalError("Cannot hash remember-me token");
        }

    }
}
