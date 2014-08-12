package com.taskroo.authn.domain;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Objects;

public class UserCredentials {
    private final String username;
    private final String password;
    private final boolean rememberMe;

    @JsonCreator
    public UserCredentials(@JsonProperty("username") String username, @JsonProperty("password") String password,
                           @JsonProperty("rememberMe") boolean rememberMe) {
        this.username = Objects.requireNonNull(username);
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }
}
